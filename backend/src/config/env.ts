import dotenv from "dotenv";
import { z } from "zod";

dotenv.config();

const envSchema = z.object({
  NODE_ENV: z.enum(["development", "production", "test"]).default("production"),
  PORT: z.string().transform(Number).default("5000"),
  
  // Security & Allowed Origins
  ALLOWED_ORIGINS: z.string().default("https://smartone-iptv.com,https://admin.smartone-iptv.com"),
  
  // Official Mobile Money Merchants (Haiti)
  NATCASH_MERCHANT_PHONE: z.string().min(8).default("+50900000000"),
  NATCASH_MERCHANT_ID: z.string().min(1).default("natcash_merchant_default"),
  NATCASH_API_SECRET: z.string().min(16).default("natcash_secret_placeholder_key_32chars"),
  
  MONCASH_MERCHANT_PHONE: z.string().min(8).default("+50900000000"),
  MONCASH_CLIENT_ID: z.string().min(1).default("moncash_client_id_default"),
  MONCASH_SECRET_KEY: z.string().min(16).default("moncash_secret_placeholder_key_32chars"),
  
  // NOWPayments Official Crypto Gateway Credentials
  NOWPAYMENTS_API_KEY: z.string().min(10).default("nowpayments_api_key_placeholder"),
  NOWPAYMENTS_IPN_SECRET: z.string().min(10).default("nowpayments_ipn_secret_placeholder"),
  NOWPAYMENTS_API_URL: z.string().url().default("https://api.nowpayments.io/v1"),

  // App2 (SmartOne IPTV) Dedicated Credentials
  APP2_NOWPAYMENTS_API_KEY: z.string().default(process.env.APP2_NOWPAYMENTS_API_KEY || process.env.NOWPAYMENTS_API_KEY || "nowpayments_api_key_placeholder"),
  APP2_NOWPAYMENTS_IPN_SECRET: z.string().default(process.env.APP2_NOWPAYMENTS_IPN_SECRET || process.env.NOWPAYMENTS_IPN_SECRET || "nowpayments_ipn_secret_placeholder"),
  APP2_NOWPAYMENTS_BASE_URL: z.string().default(process.env.APP2_NOWPAYMENTS_BASE_URL || "https://api.nowpayments.io/v1"),
  APP2_BASE_URL: z.string().default(process.env.APP2_BASE_URL || "https://smartone-iptv.com"),

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
