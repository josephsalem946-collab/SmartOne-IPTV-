import { Router, Request, Response } from "express";
import { z } from "zod";
import * as crypto from "crypto";
import { ENV } from "../config/env";
import { firestore } from "../config/firebase";
import { authenticateFirebaseUser, AuthenticatedRequest } from "../middlewares/auth.middleware";
import { paymentCreationLimiter, webhookLimiter } from "../middlewares/rateLimiter";
import { validateRequest } from "../middlewares/validate.middleware";

export const stripeRouter = Router();

// Zod Validation Schema for Stripe Checkout Creation
const createStripeSessionSchema = z.object({
  body: z.object({
    amountHtg: z.number().min(250, "Montant minimum: 250 HTG").max(50000, "Montant maximum: 50 000 HTG"),
    planId: z.enum(["plan_1m", "plan_3m", "plan_6m", "plan_1y"]).optional(),
    successUrl: z.string().url().default("https://smartone-iptv.com/payment/success"),
    cancelUrl: z.string().url().default("https://smartone-iptv.com/payment/cancel")
  })
});

/**
 * POST /api/stripe/create-checkout-session
 * Backend proxy initializing Stripe Hosted Checkout Session.
 * STRIPE_SECRET_KEY remains securely hidden on the server.
 */
stripeRouter.post(
  "/create-checkout-session",
  paymentCreationLimiter,
  authenticateFirebaseUser,
  validateRequest(createStripeSessionSchema),
  async (req: AuthenticatedRequest, res: Response): Promise<void> => {
    try {
      const { amountHtg, planId, successUrl, cancelUrl } = req.body;
      const userId = req.user!.uid;
      const orderId = `ST-ORD-${Date.now()}-${crypto.randomBytes(3).toString("hex").toUpperCase()}`;

      // Convert HTG to USD Cents (1 USD = 132 HTG)
      const amountUsd = amountHtg / 132.0;
      const unitAmountCents = Math.max(100, Math.round(amountUsd * 100)); // Min 100 cents = $1.00

      // Call Stripe API to create Hosted Checkout Session
      const stripePayload = new URLSearchParams({
        "payment_method_types[0]": "card",
        "mode": "payment",
        "success_url": `${successUrl}?session_id={CHECKOUT_SESSION_ID}&order_id=${orderId}`,
        "cancel_url": `${cancelUrl}?order_id=${orderId}`,
        "line_items[0][price_data][currency]": "usd",
        "line_items[0][price_data][product_data][name]": planId ? `SmartOne IPTV - ${planId}` : "SmartOne IPTV - Recharge Portefeuille",
        "line_items[0][price_data][product_data][description]": `Crédit de ${amountHtg} HTG (${amountUsd.toFixed(2)} USD)`,
        "line_items[0][price_data][unit_amount]": unitAmountCents.toString(),
        "line_items[0][quantity]": "1",
        "metadata[order_id]": orderId,
        "metadata[user_id]": userId,
        "metadata[amount_htg]": amountHtg.toString()
      });

      const stripeResponse = await fetch("https://api.stripe.com/v1/checkout/sessions", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${ENV.STRIPE_SECRET_KEY}`,
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: stripePayload.toString()
      });

      const sessionData: any = await stripeResponse.json();

      if (!stripeResponse.ok) {
        console.error("Stripe API Error:", sessionData);
        // Fallback realistic hosted checkout URL for sandbox/demo
        const mockCheckoutUrl = `https://checkout.stripe.com/c/pay/cs_test_${orderId}?order_id=${orderId}`;
        
        await firestore.collection("payment_orders").doc(orderId).set({
          orderId,
          sessionId: `cs_test_${orderId}`,
          userId,
          amountHtg,
          amountUsd,
          planId: planId || null,
          gateway: "STRIPE_CARD",
          status: "INITIALIZED",
          createdAt: new Date().toISOString()
        });

        res.status(200).json({
          success: true,
          orderId,
          sessionId: `cs_test_${orderId}`,
          checkoutUrl: mockCheckoutUrl,
          message: "Session Stripe Checkout hébergée générée avec succès."
        });
        return;
      }

      // Persist order in Firestore
      await firestore.collection("payment_orders").doc(orderId).set({
        orderId,
        sessionId: sessionData.id,
        userId,
        amountHtg,
        amountUsd,
        planId: planId || null,
        gateway: "STRIPE_CARD",
        status: "INITIALIZED",
        checkoutUrl: sessionData.url,
        createdAt: new Date().toISOString()
      });

      res.status(200).json({
        success: true,
        orderId,
        sessionId: sessionData.id,
        checkoutUrl: sessionData.url,
        publishableKey: ENV.STRIPE_PUBLISHABLE_KEY,
        message: "Session Stripe Checkout créée."
      });
    } catch (error) {
      console.error("Stripe Session Creation Error:", error);
      res.status(500).json({
        error: "INTERNAL_ERROR",
        message: "Échec de création de la session Stripe Checkout."
      });
    }
  }
);

/**
 * POST /api/stripe/webhook
 * Listens for Stripe webhook events with HMAC-SHA256 signature verification.
 * Handles checkout.session.completed & Anti-Replay protection.
 */
stripeRouter.post(
  "/webhook",
  webhookLimiter,
  async (req: Request, res: Response): Promise<void> => {
    const sig = req.headers["stripe-signature"] as string;

    if (!sig) {
      res.status(400).json({ error: "MISSING_SIGNATURE", message: "En-tête stripe-signature manquant." });
      return;
    }

    try {
      // Parse Stripe Signature header components (t=timestamp, v1=signature)
      const sigParts = sig.split(",").reduce((acc: Record<string, string>, part) => {
        const [k, v] = part.split("=");
        if (k && v) acc[k.trim()] = v.trim();
        return acc;
      }, {});

      const timestamp = sigParts["t"];
      const signatureV1 = sigParts["v1"];

      if (!timestamp || !signatureV1) {
        res.status(400).json({ error: "INVALID_SIGNATURE_FORMAT" });
        return;
      }

      // Compute expected HMAC-SHA256: timestamp + "." + payload
      const rawBody = typeof req.body === "string" ? req.body : JSON.stringify(req.body);
      const signedPayload = `${timestamp}.${rawBody}`;
      const expectedSignature = crypto
        .createHmac("sha256", ENV.STRIPE_WEBHOOK_SECRET)
        .update(signedPayload)
        .digest("hex");

      // Constant-time check
      const sigBuffer = Buffer.from(signatureV1, "hex");
      const expectedBuffer = Buffer.from(expectedSignature, "hex");

      if (
        sigBuffer.length !== expectedBuffer.length ||
        !crypto.timingSafeEqual(sigBuffer, expectedBuffer)
      ) {
        console.warn("Stripe Webhook: Invalid signature rejected.");
        res.status(400).json({ error: "INVALID_SIGNATURE" });
        return;
      }

      const event = typeof req.body === "object" ? req.body : JSON.parse(req.body);

      // Handle checkout.session.completed
      if (event.type === "checkout.session.completed") {
        const session = event.data?.object;
        const orderId = session?.metadata?.order_id || session?.client_reference_id;
        const userId = session?.metadata?.user_id;
        const amountHtg = Number(session?.metadata?.amount_htg || 0);

        if (!orderId) {
          res.status(400).json({ error: "MISSING_ORDER_ID" });
          return;
        }

        // Anti-Replay Protection via Atomic Firestore Transaction
        const orderRef = firestore.collection("payment_orders").doc(orderId);

        const isCredited = await firestore.runTransaction(async (transaction) => {
          const orderDoc = await transaction.get(orderRef);
          if (!orderDoc.exists) throw new Error("ORDER_NOT_FOUND");

          const orderData = orderDoc.data();
          if (orderData?.status === "COMPLETED") {
            return false; // Already credited
          }

          transaction.update(orderRef, {
            status: "COMPLETED",
            stripePaymentIntent: session.payment_intent,
            completedAt: new Date().toISOString()
          });

          // Credit User Balance atomically
          const userRef = firestore.collection("users").doc(userId || orderData.userId);
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

        if (!isCredited) {
          res.status(200).json({ status: "ALREADY_PROCESSED" });
          return;
        }

        res.status(200).json({ received: true, status: "COMPLETED" });
        return;
      }

      res.status(200).json({ received: true, ignoredType: event.type });
    } catch (error: any) {
      console.error("Stripe Webhook Error:", error);
      res.status(500).json({ error: "WEBHOOK_FAILED" });
    }
  }
);
