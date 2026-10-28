import express, { Request, Response, NextFunction } from "express";
import helmet from "helmet";
import cors from "cors";
import { ENV } from "./config/env";
import { globalLimiter } from "./middlewares/rateLimiter";
import { paymentRouter } from "./routes/payment.routes";
import { nowPaymentsRouter } from "./routes/nowpayments.routes";
import { stripeRouter } from "./routes/stripe.routes";

const app = express();

// 1. Strict Security Headers via Helmet
app.use(
  helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: ["'self'"],
        styleSrc: ["'self'", "'unsafe-inline'"],
        imgSrc: ["'self'", "data:", "https:"],
        connectSrc: ["'self'", "https://moncashbutton.digicelgroup.com", "https://natpay.natcom.com.ht"]
      }
    },
    crossOriginEmbedderPolicy: false
  })
);

// 2. Strict CORS Configuration (Only authorized origins)
const allowedOrigins = ENV.ALLOWED_ORIGINS.split(",").map((o) => o.trim());

app.use(
  cors({
    origin: (origin, callback) => {
      // Allow requests with no origin (like mobile native apps or curl in development)
      if (!origin || allowedOrigins.includes(origin)) {
        callback(null, true);
      } else {
        callback(new Error("CORS_NOT_ALLOWED"));
      }
    },
    methods: ["GET", "POST", "PUT", "DELETE"],
    allowedHeaders: ["Content-Type", "Authorization"],
    credentials: true
  })
);

// 3. Body Parsing with Strict Payload Limits (Prevent Payload DoS)
app.use(express.json({ limit: "10kb" }));
app.use(express.urlencoded({ extended: true, limit: "10kb" }));

// 4. Global Anti-DoS Rate Limiting
app.use("/api", globalLimiter);

// 5. Health Check
app.get("/health", (_req, res) => {
  res.status(200).json({ status: "HEALTHY", timestamp: new Date().toISOString() });
});

// 6. Mount Proxy Routers
app.use("/api/payment", paymentRouter);
app.use("/api/payment/nowpayments", nowPaymentsRouter);
app.use("/api/v1/payments/nowpayments", nowPaymentsRouter);
app.use("/api/stripe", stripeRouter);

// 7. Centralized Error Handling (No stack traces leaked to clients)
app.use((err: any, _req: Request, res: Response, _next: NextFunction) => {
  console.error("Unhandled Application Error:", err);

  if (err.message === "CORS_NOT_ALLOWED") {
    res.status(403).json({
      error: "CORS_BLOCKED",
      message: "Origine non autorisée par la politique de sécurité."
    });
    return;
  }

  res.status(500).json({
    error: "INTERNAL_SERVER_ERROR",
    message: "Une erreur inattendue est survenue. Veuillez contacter le support."
  });
});

// Start Server
app.listen(ENV.PORT, () => {
  console.log(`[SmartOne IPTV Backend] Serveur sécurisé actif sur le port ${ENV.PORT} (${ENV.NODE_ENV})`);
});
