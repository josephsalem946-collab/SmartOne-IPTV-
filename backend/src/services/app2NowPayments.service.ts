import * as crypto from "crypto";
import { Request } from "express";
import { ENV } from "../config/env";
import { firestore } from "../config/firebase";

export interface CreatePaymentInvoiceInput {
  amount: number;
  payCurrency?: string;
  userId: string;
  description?: string;
}

export interface PaymentInvoiceResult {
  success: boolean;
  orderId: string;
  paymentId?: string;
  invoiceUrl?: string;
  payAddress?: string;
  payAmount?: number;
  payCurrency?: string;
  amountUsd: number;
  amountHtg: number;
  message?: string;
}

/**
 * Fonksyon pou triye tout kle yon objè alfabetikman (ak anndan l tou si l genyen lòt objè)
 */
export function sortObjectAlphabetically(obj: any): any {
  if (obj === null || typeof obj !== "object") {
    return obj;
  }
  if (Array.isArray(obj)) {
    return obj.map(sortObjectAlphabetically);
  }
  return Object.keys(obj)
    .sort()
    .reduce((result: Record<string, any>, key: string) => {
      result[key] = sortObjectAlphabetically(obj[key]);
      return result;
    }, {});
}

/**
 * App2NowPaymentsService
 * Service dedye pou jere tout peman NOWPayments pou SmartOne IPTV (App2)
 */
export class App2NowPaymentsService {
  public static readonly config = {
    appName: "SmartOne IPTV",
    appPrefix: "App2",
    version: "1.0.0",
    nowPaymentsApiKey: ENV.APP2_NOWPAYMENTS_API_KEY,
    nowPaymentsIpnSecret: ENV.APP2_NOWPAYMENTS_IPN_SECRET,
    apiBaseUrl: (ENV.APP2_NOWPAYMENTS_BASE_URL || "https://api.nowpayments.io/v1").replace(/\/+$/, ""),
    ipnCallbackUrl: `${ENV.APP2_BASE_URL}/api/app2/nowpayments/ipn`,
    successUrl: `${ENV.APP2_BASE_URL}/app2/payment/success`,
    cancelUrl: `${ENV.APP2_BASE_URL}/app2/payment/cancel`
  };

  /**
   * Kreye yon fakti peman nouvo pou SmartOne IPTV
   * Jenera order_id ak fòma APP2-IPTV-[TIMESTAMP]-[USER_ID]
   */
  public static async createPaymentInvoice(
    data: CreatePaymentInvoiceInput
  ): Promise<PaymentInvoiceResult> {
    const timestamp = Date.now();
    const cleanUserId = (data.userId || "GUEST").replace(/[^a-zA-Z0-9_-]/g, "");
    const orderId = `APP2-IPTV-${timestamp}-${cleanUserId}`;

    const amountUsd = Number(data.amount);
    const amountHtg = amountUsd * 132.0; // 1 USD = 132 HTG
    const payCurrency = data.payCurrency || "usdttrc20";
    const orderDescription = data.description || "Abònman VIP SmartOne IPTV";

    const payloadStructure = {
      price_amount: amountUsd,
      price_currency: "usd",
      pay_currency: payCurrency,
      order_id: orderId,
      order_description: orderDescription,
      ipn_callback_url: this.config.ipnCallbackUrl,
      success_url: this.config.successUrl,
      cancel_url: this.config.cancelUrl,
      is_fee_paid_by_user: true
    };

    console.log(`[App2NowPaymentsService] Kreye fakti peman pou order_id: ${orderId}, Montan: $${amountUsd} USD`);

    const response = await fetch(`${this.config.apiBaseUrl}/payment`, {
      method: "POST",
      headers: {
        "x-api-key": this.config.nowPaymentsApiKey,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payloadStructure)
    });

    const responseData: any = await response.json();

    if (!response.ok) {
      console.error("[App2NowPaymentsService] NOWPayments Gateway Error:", responseData);
      throw new Error(responseData.message || "Erè pandan kreyasyon fakti NOWPayments");
    }

    // Persiste kòmand lan nan Firestore pou SmartOne IPTV
    await firestore.collection("payment_orders").doc(orderId).set({
      orderId,
      paymentId: responseData.payment_id?.toString() || null,
      userId: cleanUserId,
      appPrefix: "App2",
      appName: "SmartOne IPTV",
      amountUsd,
      amountHtg,
      payCurrency,
      payAddress: responseData.pay_address || null,
      payAmount: responseData.pay_amount || null,
      gateway: "NOWPAYMENTS_APP2",
      status: "INITIALIZED",
      orderDescription,
      createdAt: new Date().toISOString()
    });

    return {
      success: true,
      orderId,
      paymentId: responseData.payment_id?.toString(),
      invoiceUrl: responseData.invoice_url,
      payAddress: responseData.pay_address,
      payAmount: responseData.pay_amount,
      payCurrency: responseData.pay_currency || payCurrency,
      amountUsd,
      amountHtg,
      message: "Fakti peman SmartOne IPTV kreye avèk siksè."
    };
  }

  /**
   * Verifye si siyati IPN nan valide pou sekirite lè l sèvi avèk HMAC sha512
   */
  public static verifyApp2IpnSignature(req: Request): boolean {
    const sigHeader = req.headers["x-nowpayments-sig"] as string;
    if (!sigHeader) {
      console.warn("[App2NowPaymentsService] Header x-nowpayments-sig manke!");
      return false;
    }

    const secret = this.config.nowPaymentsIpnSecret;
    if (!secret) {
      console.error("[App2NowPaymentsService] APP2_NOWPAYMENTS_IPN_SECRET pa konfigire!");
      return false;
    }

    try {
      const sortedPayload = sortObjectAlphabetically(req.body);
      const stringifiedPayload = JSON.stringify(sortedPayload);

      const hmac = crypto.createHmac("sha512", secret);
      hmac.update(stringifiedPayload);
      const calculatedSig = hmac.digest("hex");

      const receivedBuffer = Buffer.from(sigHeader, "hex");
      const calculatedBuffer = Buffer.from(calculatedSig, "hex");

      if (receivedBuffer.length !== calculatedBuffer.length) {
        return false;
      }

      return crypto.timingSafeEqual(receivedBuffer, calculatedBuffer);
    } catch (err) {
      console.error("[App2NowPaymentsService] Erè pandan verifikasyon siyati:", err);
      return false;
    }
  }

  /**
   * Trete IPN pou SmartOne IPTV epi aktive abònman oswa mete ajou balans lan
   */
  public static async processIpnPayment(body: any): Promise<{
    processed: boolean;
    status: string;
    message: string;
  }> {
    const { order_id, payment_status, actually_paid, price_amount, pay_currency } = body;

    // 1. Asire order_id la kòmanse ak APP2-IPTV- oswa APP2_
    if (!order_id || (typeof order_id === "string" && !order_id.startsWith("APP2"))) {
      console.log(`[App2NowPaymentsService] Order "${order_id}" pa pou App2. Ignorasyon san modifikasyon.`);
      return {
        processed: false,
        status: "NOT_APP2",
        message: "Order ID pa koresponn ak SmartOne IPTV (App2)"
      };
    }

    console.log(`[App2NowPaymentsService] Resevwa IPN pou ${order_id}, Status: ${payment_status}`);

    // Konsève log nan ipn_logs
    await firestore.collection("ipn_logs").add({
      orderId: order_id,
      appPrefix: "App2",
      gateway: "NOWPAYMENTS",
      paymentStatus: payment_status,
      actuallyPaid: actually_paid || null,
      priceAmount: price_amount || null,
      payCurrency: pay_currency || null,
      rawPayload: body,
      receivedAt: new Date().toISOString()
    });

    // Se sèlman lè estati a se 'finished' oswa 'confirmed' nou aktive sèvis la
    if (payment_status !== "finished" && payment_status !== "confirmed") {
      console.log(`[App2NowPaymentsService] Lòd ${order_id} nan estati ${payment_status}. Nou ap tann konfimasyon final.`);
      return {
        processed: true,
        status: payment_status,
        message: `Peman an nan estati: ${payment_status}`
      };
    }

    // Tranzaksyon atomik pou evite doub kredi (Anti-Replay)
    const orderRef = firestore.collection("payment_orders").doc(order_id);

    await firestore.runTransaction(async (transaction) => {
      const orderDoc = await transaction.get(orderRef);
      if (!orderDoc.exists) {
        console.warn(`[App2NowPaymentsService] Lòd ${order_id} pa jwenn nan baz done a.`);
        return;
      }

      const orderData = orderDoc.data();
      if (orderData?.status === "COMPLETED") {
        console.log(`[App2NowPaymentsService] Lòd ${order_id} te deja trete avèk siksè.`);
        return;
      }

      // Mete a jou estati lòd la
      transaction.update(orderRef, {
        status: "COMPLETED",
        paymentStatus: payment_status,
        actuallyPaid: actually_paid || orderData?.amountUsd,
        updatedAt: new Date().toISOString()
      });

      const userId = orderData?.userId;
      const amountHtg = orderData?.amountHtg || (orderData?.amountUsd * 132.0);

      if (userId && userId !== "GUEST") {
        const userRef = firestore.collection("users").doc(userId);
        const userDoc = await transaction.get(userRef);

        if (userDoc.exists) {
          // Kredite balans itilizatè a
          const currentBalance = (userDoc.data()?.balance as number) || 0.0;
          transaction.update(userRef, {
            balance: currentBalance + amountHtg,
            isVip: true,
            lastPaymentOrderId: order_id,
            updatedAt: new Date().toISOString()
          });
        }

        // Aktive Abònman VIP SmartOne IPTV nan koleksyon abònman yo
        const subRef = firestore.collection("smartone_subscriptions").doc(`${userId}_active`);
        transaction.set(subRef, {
          userId,
          orderId: order_id,
          appName: "SmartOne IPTV",
          appPrefix: "App2",
          status: "ACTIVE",
          planLabel: "Abonnement VIP SmartOne IPTV",
          priceHtg: amountHtg,
          activatedAt: new Date().toISOString()
        }, { merge: true });
      }
    });

    console.log(`[App2NowPaymentsService] ✅ Abònman VIP aktive epi balans kredite avèk siksè pou ${order_id}`);

    return {
      processed: true,
      status: "COMPLETED",
      message: "Peman konfime, abònman VIP aktive avèk siksè."
    };
  }
}
