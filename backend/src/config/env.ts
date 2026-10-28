import dotenv from "dotenv";
import { z } from "zod";

dotenv.config();

const envSchema = z.object({
  NODE_ENV: z.enum(["development", "production", "test"]).default("production"),
  PORT: z.string().transform(Number).default("5000"),
  
  // Security & Allowed Origins
  ALLOWED_ORIGINS: z.string().default("https://smartone-iptv.com,https://admin.smartone-iptv.com"),
  
  // Official Mobile Money Merchants (Haiti)
  NATCASH_MERCHANT_PHONE: z.string().min(8).default("+50932153281"),
  NATCASH_MERCHANT_ID: z.string().min(1),
  NATCASH_API_SECRET: z.string().min(16),
  
  MONCASH_MERCHANT_PHONE: z.string().min(8).default("+50946877695"),
  MONCASH_CLIENT_ID: z.string().min(1),
  MONCASH_SECRET_KEY: z.string().min(16),
  
  // NOWPayments Official Crypto Gateway Credentials
  NOWPAYMENTS_API_KEY: z.string().min(10).default("6EZ44P4-K52MBVT-QQ56MRX-WZWM8J5"),
  NOWPAYMENTS_IPN_SECRET: z.string().min(10).default("hKiNfKED5A2JTxpwa1oR35Lp2XRzl9Ez"),
  NOWPAYMENTS_API_URL: z.string().url().default("https://api.nowpayments.io/v1"),

  // Stripe Official Credentials
  STRIPE_SECRET_KEY: z.string().min(10).default("sk_test_smartone_default_secret"),
  STRIPE_PUBLISHABLE_KEY: z.string().min(10).default("pk_test_smartone_default_pub"),
  STRIPE_WEBHOOK_SECRET: z.string().min(10).default("whsec_smartone_default_secret"),

  // Firebase Admin Credentials
  FIREBASE_PROJECT_ID: z.string().min(1),
  FIREBASE_CLIENT_EMAIL: z.string().email(),
  FIREBASE_PRIVATE_KEY: z.string().min(1)
});

// Fail fast on startup if any critical variable is missing or malformed
const parsedEnv = envSchema.safeParse(process.env);

if (!parsedEnv.success) {
  console.error("FATAL: Environment configuration validation failed:");
  console.error(JSON.stringify(parsedEnv.error.format(), null, 2));
  process.exit(1);
}

export const ENV = parsedEnv.data;
