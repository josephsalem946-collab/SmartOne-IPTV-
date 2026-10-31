import { Router, Request, Response } from "express";
import { z } from "zod";
import { App2NowPaymentsService } from "../services/app2NowPayments.service";
import { paymentCreationLimiter, webhookLimiter } from "../middlewares/rateLimiter";
import { validateRequest } from "../middlewares/validate.middleware";

export const app2Router = Router();

const createPaymentSchema = z.object({
  body: z.object({
    amount: z.number().min(5, "Montan minimòm se $5.00 USD"),
    payCurrency: z.string().default("usdttrc20"),
    userId: z.string().min(1, "User ID obligatwa"),
    description: z.string().optional()
  })
});

/**
 * POST /api/app2/create-payment
 * Wout pou inisye yon peman pou SmartOne IPTV
 */
app2Router.post(
  "/create-payment",
  paymentCreationLimiter,
  validateRequest(createPaymentSchema),
  async (req: Request, res: Response): Promise<void> => {
    try {
      const { amount, payCurrency, userId, description } = req.body;

      const invoice = await App2NowPaymentsService.createPaymentInvoice({
        amount,
        payCurrency,
        userId,
        description
      });

      res.status(200).json({
        success: true,
        data: invoice
      });
    } catch (error: any) {
      console.error("[app2Router] Erè kreye peman:", error);
      res.status(500).json({
        success: false,
        error: "PAYMENT_CREATION_FAILED",
        message: error.message || "Erè pandan inisyalizasyon peman an."
      });
    }
  }
);

/**
 * POST /api/app2/nowpayments/ipn
 * Wout IPN Webhook pou konfime peman an otomatikman epi aktive abònman an
 */
app2Router.post(
  "/nowpayments/ipn",
  webhookLimiter,
  async (req: Request, res: Response): Promise<void> => {
    try {
      // 1. Verifikasyon Sekirite Siyati HMAC-SHA512
      const isValidSig = App2NowPaymentsService.verifyApp2IpnSignature(req);

      if (!isValidSig) {
        console.warn("[app2Router] ⚠️ Atansyon: Siyati IPN x-nowpayments-sig pa valab!");
        res.status(400).json({
          error: "INVALID_SIGNATURE",
          message: "Siyati IPN NOWPayments la pa valab oswa manke."
        });
        return;
      }

      // 2. Tretman peman an ak aktivasyon abònman VIP
      const result = await App2NowPaymentsService.processIpnPayment(req.body);

      // Toujou retounen 200 pou NOWPayments konnen webhook la trete san pwoblèm
      res.status(200).json({
        success: true,
        result
      });
    } catch (error: any) {
      console.error("[app2Router] Erè pandan tretman IPN:", error);
      res.status(500).json({
        error: "IPN_PROCESSING_ERROR",
        message: "Erè entèn pandan tretman IPN."
      });
    }
  }
);
