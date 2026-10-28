import { Router, Request, Response } from "express";
import { z } from "zod";
import * as crypto from "crypto";
import { ENV } from "../config/env";
import { firestore } from "../config/firebase";
import { authenticateFirebaseUser, AuthenticatedRequest } from "../middlewares/auth.middleware";
import { paymentCreationLimiter, webhookLimiter } from "../middlewares/rateLimiter";
import { validateRequest } from "../middlewares/validate.middleware";

export const nowPaymentsRouter = Router();

// Zod Schema for initiating NOWPayments crypto payment
const createCryptoPaymentSchema = z.object({
  body: z.object({
    amountUsd: z.number().min(10, "Minimum $10 USD").max(100, "Maximum $100 USD par 24h"),
    payCurrency: z.enum(["usdttrc20", "btc", "eth", "trx", "ltc", "bnb"]).default("usdttrc20"),
    planId: z.enum(["plan_1m", "plan_3m", "plan_6m", "plan_1y"]).optional(),
    customerEmail: z.string().email().optional()
  })
});

/**
 * Helper to recursively sort object keys alphabetically for NOWPayments HMAC-SHA512 verification.
 */
export function sortObjectAlphabetically(obj: any): any {
  if (typeof obj !== "object" || obj === null) return obj;
  if (Array.isArray(obj)) return obj.map(sortObjectAlphabetically);
  return Object.keys(obj)
    .sort()
    .reduce((sorted: Record<string, any>, key: string) => {
      sorted[key] = sortObjectAlphabetically(obj[key]);
      return sorted;
    }, {});
}

/**
 * Helper to generate order_id with the APP2_ prefix for SmartOne IPTV.
 * Ensures strict separation from Application 1 (e.g. Gain Cash) sharing
 * the same NOWPayments API Key and Webhook/IPN URL.
 * Example outputs: APP2_1697283940_A89F, APP2_TXT_9876
 */
export function generateSmartOneOrderId(prefix: string = "APP2_"): string {
  const timestamp = Math.floor(Date.now() / 1000);
  const randomHex = crypto.randomBytes(2).toString("hex").toUpperCase();
  return `${prefix}${timestamp}_${randomHex}`;
}

/**
 * POST /api/payment/nowpayments/create
 * Initiates a cryptocurrency payment via official NOWPayments API.
 * Keeps NOWPAYMENTS_API_KEY completely hidden from the client.
 */
nowPaymentsRouter.post(
  "/create",
  paymentCreationLimiter,
  authenticateFirebaseUser,
  validateRequest(createCryptoPaymentSchema),
  async (req: AuthenticatedRequest, res: Response): Promise<void> => {
    try {
      const { amountUsd, payCurrency, planId, customerEmail } = req.body;
      const userId = req.user!.uid;
      // Generates order_id strictly prefixed with APP2_ for SmartOne IPTV
      const orderId = generateSmartOneOrderId("APP2_");
      const amountHtg = amountUsd * 132.0; // Current exchange rate: 1 USD = 132 HTG

      // Prepare request payload for NOWPayments Invoice/Payment API
      const nowPaymentsPayload = {
        price_amount: amountUsd,
        price_currency: "usd",
        pay_currency: payCurrency,
        ipn_callback_url: `https://api.smartone-iptv.com/api/payment/nowpayments/ipn`,
        order_id: orderId,
        order_description: `Recharge Portefeuille SmartOne IPTV - $${amountUsd} USD (${amountHtg} HTG)`,
        is_fee_paid_by_user: true
      };

      // Call NOWPayments API
      const response = await fetch(`${ENV.NOWPAYMENTS_API_URL}/payment`, {
        method: "POST",
        headers: {
          "x-api-key": ENV.NOWPAYMENTS_API_KEY,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(nowPaymentsPayload)
      });

      const responseData: any = await response.json();

      if (!response.ok) {
        console.error("NOWPayments API Error:", responseData);
        res.status(502).json({
          error: "GATEWAY_ERROR",
          message: responseData.message || "Erreur de communication avec NOWPayments."
        });
        return;
      }

      // Persist order in Firestore
      await firestore.collection("payment_orders").doc(orderId).set({
        orderId,
        paymentId: responseData.payment_id?.toString() || null,
        userId,
        amountUsd,
        amountHtg,
        planId: planId || null,
        payCurrency,
        payAddress: responseData.pay_address,
        payAmount: responseData.pay_amount,
        gateway: "NOWPAYMENTS_CRYPTO",
        status: "INITIALIZED",
        createdAt: new Date().toISOString()
      });

      res.status(200).json({
        success: true,
        orderId,
        paymentId: responseData.payment_id,
        payAddress: responseData.pay_address,
        payAmount: responseData.pay_amount,
        payCurrency: responseData.pay_currency,
        amountHtg,
        message: "Paiement crypto initialisé avec succès."
      });
    } catch (error) {
      console.error("Error creating NOWPayments order:", error);
      res.status(500).json({
        error: "INTERNAL_ERROR",
        message: "Échec de l'initialisation du paiement crypto."
      });
    }
  }
);

/**
 * POST /api/payment/nowpayments/ipn
 * Strict IPN Webhook verification using HMAC-SHA512 with sorted JSON keys.
 * Rejects invalid or missing signatures with 400 or 403.
 * Anti-replay protection prevents duplicate crediting.
 */
nowPaymentsRouter.post(
  "/ipn",
  webhookLimiter,
  async (req: Request, res: Response): Promise<void> => {
    const receivedSig = req.headers["x-nowpayments-sig"] as string;

    if (!receivedSig) {
      res.status(400).json({ error: "MISSING_SIGNATURE", message: "Header x-nowpayments-sig manquant." });
      return;
    }

    try {
      // 1. Sort request body keys alphabetically
      const sortedBody = sortObjectAlphabetically(req.body);
      const rawString = JSON.stringify(sortedBody);

      // 2. Compute HMAC-SHA512 using NOWPAYMENTS_IPN_SECRET
      const calculatedSig = crypto
        .createHmac("sha512", ENV.NOWPAYMENTS_IPN_SECRET)
        .update(rawString)
        .digest("hex");

      // 3. Constant-time comparison
      const receivedBuffer = Buffer.from(receivedSig, "hex");
      const calculatedBuffer = Buffer.from(calculatedSig, "hex");

      if (
        receivedBuffer.length !== calculatedBuffer.length ||
        !crypto.timingSafeEqual(receivedBuffer, calculatedBuffer)
      ) {
        console.warn("NOWPayments IPN: Invalid signature detected. Request rejected.");
        res.status(403).json({ error: "INVALID_SIGNATURE", message: "Signature cryptographique invalide." });
        return;
      }

      // 4. Verify payment status & Multi-App routing
      const { payment_id, payment_status, order_id, actually_paid, price_amount } = req.body;

      // SmartOne IPTV Multi-App Isolation:
      // Verify if order_id is prefixed with APP2_. If not, this webhook notification belongs to App 1 (e.g. Gain Cash).
      if (typeof order_id === "string" && !order_id.startsWith("APP2_")) {
        console.log(`[NOWPayments IPN Router] Order "${order_id}" belongs to App 1 / External app (non-APP2). Acknowledging without interfering with SmartOne DB.`);
        await firestore.collection("ipn_logs").add({
          orderId: order_id,
          appTarget: "EXTERNAL_APP1",
          gateway: "NOWPAYMENTS",
          paymentStatus: payment_status,
          rawPayload: req.body,
          receivedAt: new Date().toISOString()
        });

        res.status(200).json({
          status: "ROUTED_TO_APP1",
          message: `Order ${order_id} identified as App 1 payment. Acknowledged.`
        });
        return;
      }

      // Only credit when status is finished or confirmed
      if (payment_status !== "finished" && payment_status !== "confirmed") {
        console.log(`NOWPayments IPN: Order ${order_id} status is ${payment_status}. Waiting for completion.`);
        res.status(200).json({ status: "ACKNOWLEDGED", currentStatus: payment_status });
        return;
      }

      // Handle partially paid (Underpaid) payments
      if (payment_status === "partially_paid") {
        console.warn(`NOWPayments IPN: Underpaid alert for order ${order_id}. Actually paid: ${actually_paid}`);
        await firestore.collection("payment_orders").doc(order_id).update({
          status: "PARTIALLY_PAID",
          actuallyPaid: actually_paid,
          updatedAt: new Date().toISOString()
        });

        // Audit Logging
        await firestore.collection("ipn_logs").add({
          orderId: order_id,
          gateway: "NOWPAYMENTS",
          paymentStatus: "partially_paid",
          rawPayload: req.body,
          createdAt: new Date().toISOString()
        });

        res.status(200).json({ status: "PARTIALLY_PAID", message: "Montan voye a pa konplè." });
        return;
      }

      // 5. Anti-Replay Protection via Atomic Firestore Transaction
      const orderRef = firestore.collection("payment_orders").doc(order_id);

      const isCredited = await firestore.runTransaction(async (transaction) => {
        const orderDoc = await transaction.get(orderRef);
        if (!orderDoc.exists) {
          throw new Error("ORDER_NOT_FOUND");
        }

        const orderData = orderDoc.data();
        if (orderData?.status === "COMPLETED") {
          return false; // Already credited
        }

        const amountHtg = orderData?.amountHtg || (Number(price_amount || 10) * 132.0);

        // Update Order to COMPLETED
        transaction.update(orderRef, {
          status: "COMPLETED",
          paymentId: payment_id?.toString(),
          actuallyPaid: actually_paid,
          completedAt: new Date().toISOString()
        });

        // Credit User Balance atomically
        const userRef = firestore.collection("users").doc(orderData?.userId);
        const userDoc = await transaction.get(userRef);

        if (userDoc.exists) {
          const currentBal = userDoc.data()?.balance || 0;
          transaction.update(userRef, {
            balance: currentBal + amountHtg,
            lastDepositAt: new Date().toISOString()
          });
        }

        return true;
      });

      // Audit Logging for completed transactions
      await firestore.collection("ipn_logs").add({
        orderId: order_id,
        gateway: "NOWPAYMENTS",
        paymentStatus: "finished",
        paymentId: payment_id?.toString(),
        amountHtg: Number(price_amount || 10) * 132.0,
        createdAt: new Date().toISOString()
      });

      if (!isCredited) {
        res.status(200).json({ status: "ALREADY_PROCESSED", message: "Ordre crypto déjà validé antérieurement." });
        return;
      }

      res.status(200).json({ status: "SUCCESS", message: "Paiement crypto NOWPayments validé et crédité." });
    } catch (error: any) {
      console.error("NOWPayments IPN Error:", error);
      if (error.message === "ORDER_NOT_FOUND") {
        res.status(404).json({ error: "ORDER_NOT_FOUND", message: "Ordre introuvable." });
      } else {
        res.status(500).json({ error: "INTERNAL_ERROR", message: "Erreur de traitement IPN." });
      }
    }
  }
);

/**
 * GET /api/payment/nowpayments/status/:orderId
 * Real-time polling endpoint for frontend countdown screen.
 */
nowPaymentsRouter.get(
  "/status/:orderId",
  authenticateFirebaseUser,
  async (req: AuthenticatedRequest, res: Response): Promise<void> => {
    try {
      const { orderId } = req.params;
      const doc = await firestore.collection("payment_orders").doc(orderId).get();

      if (!doc.exists) {
        res.status(404).json({ error: "NOT_FOUND", message: "Ordre introuvable." });
        return;
      }

      const orderData = doc.data();
      res.status(200).json({
        orderId,
        status: orderData?.status || "INITIALIZED",
        amountHtg: orderData?.amountHtg,
        actuallyPaid: orderData?.actuallyPaid,
        completedAt: orderData?.completedAt
      });
    } catch (error) {
      res.status(500).json({ error: "INTERNAL_ERROR", message: "Erreur de vérification du statut." });
    }
  }
);
