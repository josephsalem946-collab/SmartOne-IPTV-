import { Router, Response } from "express";
import { z } from "zod";
import * as crypto from "crypto";
import { ENV } from "../config/env";
import { firestore } from "../config/firebase";
import { authenticateFirebaseUser, AuthenticatedRequest } from "../middlewares/auth.middleware";
import { paymentCreationLimiter, webhookLimiter } from "../middlewares/rateLimiter";
import { validateRequest } from "../middlewares/validate.middleware";

export const paymentRouter = Router();

// Zod Validation Schema for creating payments
const createPaymentSchema = z.object({
  body: z.object({
    amount: z.number().min(250, "Montant minimum: 250 HTG").max(10000, "Montant maximum: 10 000 HTG"),
    planId: z.enum(["plan_1m", "plan_3m", "plan_6m", "plan_1y"]).optional(),
    returnUrl: z.string().url().default("https://smartone-iptv.com/payment/success"),
    cancelUrl: z.string().url().default("https://smartone-iptv.com/payment/cancel")
  })
});

// Zod Validation Schema for IPN / Webhooks
const webhookSchema = z.object({
  body: z.object({
    orderId: z.string().min(5),
    transactionId: z.string().min(5),
    amount: z.number().positive(),
    status: z.enum(["COMPLETED", "FAILED"]),
    timestamp: z.number(),
    signature: z.string().min(10)
  })
});

/**
 * POST /api/payment/moncash/create
 * Backend proxy initializing MonCash Online payment.
 * Merchant Phone (+50946877695) and API secrets remain hidden on server.
 */
paymentRouter.post(
  "/moncash/create",
  paymentCreationLimiter,
  authenticateFirebaseUser,
  validateRequest(createPaymentSchema),
  async (req: AuthenticatedRequest, res: Response): Promise<void> => {
    try {
      const { amount, planId } = req.body;
      const userId = req.user!.uid;
      const orderId = `MC-ORD-${Date.now()}-${crypto.randomBytes(3).toString("hex").toUpperCase()}`;

      // Calculate HMAC token using backend secret
      const tokenPayload = `${orderId}:${amount}:${ENV.MONCASH_MERCHANT_PHONE}`;
      const paymentToken = crypto
        .createHmac("sha256", ENV.MONCASH_SECRET_KEY)
        .update(tokenPayload)
        .digest("base64");

      const checkoutUrl = `https://moncashbutton.digicelgroup.com/Moncash-middleware/Checkout/Pay?token=${encodeURIComponent(
        paymentToken
      )}&order_id=${encodeURIComponent(orderId)}&currency=HTG`;

      // Persist order in Firestore with INITIALIZED status
      await firestore.collection("payment_orders").doc(orderId).set({
        orderId,
        userId,
        amount,
        planId: planId || null,
        gateway: "MONCASH",
        status: "INITIALIZED",
        merchantPhone: ENV.MONCASH_MERCHANT_PHONE,
        createdAt: new Date().toISOString()
      });

      res.status(200).json({
        success: true,
        orderId,
        checkoutUrl,
        message: "Lien de paiement hébergé MonCash créé avec succès."
      });
    } catch (error) {
      console.error("MonCash creation error:", error);
      res.status(500).json({
        error: "PAYMENT_INITIATION_FAILED",
        message: "Impossible d'initialiser le paiement MonCash actuellement."
      });
    }
  }
);

/**
 * POST /api/payment/natcash/create
 * Backend proxy initializing Natcash Online payment.
 * Merchant Phone (+50932153281) and API secrets remain hidden on server.
 */
paymentRouter.post(
  "/natcash/create",
  paymentCreationLimiter,
  authenticateFirebaseUser,
  validateRequest(createPaymentSchema),
  async (req: AuthenticatedRequest, res: Response): Promise<void> => {
    try {
      const { amount, planId } = req.body;
      const userId = req.user!.uid;
      const orderId = `NC-ORD-${Date.now()}-${crypto.randomBytes(3).toString("hex").toUpperCase()}`;

      // Calculate HMAC token using backend secret
      const tokenPayload = `${orderId}:${amount}:${ENV.NATCASH_MERCHANT_PHONE}`;
      const paymentToken = crypto
        .createHmac("sha256", ENV.NATCASH_API_SECRET)
        .update(tokenPayload)
        .digest("base64");

      const checkoutUrl = `https://natpay.natcom.com.ht/payment/web/checkout?token=${encodeURIComponent(
        paymentToken
      )}&order_id=${encodeURIComponent(orderId)}&currency=HTG`;

      // Persist order in Firestore with INITIALIZED status
      await firestore.collection("payment_orders").doc(orderId).set({
        orderId,
        userId,
        amount,
        planId: planId || null,
        gateway: "NATCASH",
        status: "INITIALIZED",
        merchantPhone: ENV.NATCASH_MERCHANT_PHONE,
        createdAt: new Date().toISOString()
      });

      res.status(200).json({
        success: true,
        orderId,
        checkoutUrl,
        message: "Lien de paiement hébergé Natcash créé avec succès."
      });
    } catch (error) {
      console.error("Natcash creation error:", error);
      res.status(500).json({
        error: "PAYMENT_INITIATION_FAILED",
        message: "Impossible d'initialiser le paiement Natcash actuellement."
      });
    }
  }
);

/**
 * POST /api/payment/moncash/ipn
 * Webhook handler with HMAC-SHA256 signature verification & Anti-Replay protection.
 */
paymentRouter.post(
  "/moncash/ipn",
  webhookLimiter,
  validateRequest(webhookSchema),
  async (req, res): Promise<void> => {
    await handleWebhookIPN(req.body, ENV.MONCASH_SECRET_KEY, "MONCASH", res);
  }
);

/**
 * POST /api/payment/natcash/ipn
 * Webhook handler with HMAC-SHA256 signature verification & Anti-Replay protection.
 */
paymentRouter.post(
  "/natcash/ipn",
  webhookLimiter,
  validateRequest(webhookSchema),
  async (req, res): Promise<void> => {
    await handleWebhookIPN(req.body, ENV.NATCASH_API_SECRET, "NATCASH", res);
  }
);

/**
 * Shared Core IPN Processor:
 * - Constant-time signature verification
 * - Firestore Transaction to prevent replay attacks
 * - Auto-credit wallet balance
 */
async function handleWebhookIPN(
  body: z.infer<typeof webhookSchema>["body"],
  secretKey: string,
  gateway: "MONCASH" | "NATCASH",
  res: Response
): Promise<void> {
  const { orderId, transactionId, amount, status, timestamp, signature } = body;

  // 1. Verify HMAC Signature
  const rawData = `${orderId}|${transactionId}|${amount}|${status}|${timestamp}`;
  const expectedSignature = crypto.createHmac("sha256", secretKey).update(rawData).digest("base64");

  const signatureBuffer = Buffer.from(signature);
  const expectedBuffer = Buffer.from(expectedSignature);

  if (
    signatureBuffer.length !== expectedBuffer.length ||
    !crypto.timingSafeEqual(signatureBuffer, expectedBuffer)
  ) {
    res.status(403).json({ error: "FORBIDDEN", message: "Signature IPN invalide." });
    return;
  }

  // 2. Anti-Replay Protection via Atomic Firestore Transaction
  const orderRef = firestore.collection("payment_orders").doc(orderId);

  try {
    const credited = await firestore.runTransaction(async (transaction) => {
      const orderDoc = await transaction.get(orderRef);

      if (!orderDoc.exists) {
        throw new Error("ORDER_NOT_FOUND");
      }

      const orderData = orderDoc.data();

      // Check if already processed (Anti-Replay)
      if (orderData?.status === "COMPLETED") {
        return false; // Already credited
      }

      // Update Order
      transaction.update(orderRef, {
        status: "COMPLETED",
        transactionReference: transactionId,
        completedAt: new Date().toISOString()
      });

      // Credit User Balance in atomic transaction
      const userRef = firestore.collection("users").doc(orderData?.userId);
      const userDoc = await transaction.get(userRef);

      if (userDoc.exists) {
        const currentBalance = userDoc.data()?.balance || 0;
        transaction.update(userRef, {
          balance: currentBalance + amount,
          lastDepositAt: new Date().toISOString()
        });
      }

      return true;
    });

    if (!credited) {
      res.status(200).json({ status: "ALREADY_PROCESSED", message: "Ordre déjà validé antérieurement." });
      return;
    }

    res.status(200).json({ status: "SUCCESS", message: `Paiement ${gateway} validé et crédité.` });
  } catch (err: any) {
    console.error("Webhook processing error:", err);
    if (err.message === "ORDER_NOT_FOUND") {
      res.status(404).json({ error: "NOT_FOUND", message: "Ordre de paiement introuvable." });
    } else {
      res.status(500).json({ error: "INTERNAL_ERROR", message: "Erreur de validation IPN." });
    }
  }
}
