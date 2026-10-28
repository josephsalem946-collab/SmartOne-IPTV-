# SmartOne IPTV — Architecture de Sécurité Backend & Proxy de Paiement

## 1. Vue d'ensemble de l'Architecture
Ce module backend implémente une architecture Zero-Trust pour isoler les clés d'API sensibles, déléguer l'authentification à Firebase Admin SDK, et sécuriser les passerelles de paiement MonCash et Natcash.

```
[ Application Android / Web ]
           │
           │  HTTPS + Authorization: Bearer <Firebase ID Token>
           ▼
[ Serveur Backend Proxy (Node.js / Express / TypeScript) ]
   ├── Helmet (En-têtes HTTP de sécurité & CSP)
   ├── Strict CORS (Origines autorisées uniquement)
   ├── Rate Limiting (express-rate-limit par IP & UID)
   ├── Firebase Admin SDK (Vérification cryptographique du jeton d'accès)
   ├── Zod (Validation & assainissement Zero-Trust de chaque requête)
   └── Routeurs Métier & IPN
           │
           ├──► [ MonCash Online API (Digicel) ] (Clé marchande cachée)
           ├──► [ Natcash Online API (Natcom) ] (Clé marchande cachée)
           └──► [ Cloud Firestore Database ] (Mise à jour atomique & Anti-Replay)
```

## 2. Arborescence des Dossiers
```
backend/
├── package.json                    # Dépendances de sécurité et scripts
├── tsconfig.json                   # Configuration TypeScript
├── .env.example                    # Modèle des variables d'environnement requises
└── src/
    ├── server.ts                   # Serveur Express, Helmet, CORS, Gestion globale des erreurs
    ├── config/
    │   ├── env.ts                  # Validation Zod stricte des variables au démarrage
    │   └── firebase.ts             # Initialisation de Firebase Admin SDK
    ├── middlewares/
    │   ├── auth.middleware.ts      # Validation du jeton Firebase ID (Bearer Token) & rôles
    │   ├── rateLimiter.ts          # Limitation de débit granulaire (Global, Paiements, Webhooks)
    │   └── validate.middleware.ts  # Validation et assainissement Zero-Trust des requêtes (Zod)
    └── routes/
        └── payment.routes.ts       # Routes d'initialisation MonCash/Natcash & Webhooks IPN
```

## 3. Matrice de Sécurité & Conformité OWASP Top 10

| Vulnérabilité OWASP | Mesure de Protection Implémentée |
| :--- | :--- |
| **A01: Broken Access Control** | Vérification systématique du jeton Firebase Admin (`checkRevoked = true`) et RBAC sur chaque route. |
| **A02: Cryptographic Failures** | Clés API isolées dans `.env` côté serveur. Signatures Webhook HMAC-SHA256 validées en temps constant (`timingSafeEqual`). |
| **A03: Injection (SQL/NoSQL/XSS)** | Schémas de validation stricte Zod et assainissement avant tout accès à la base de données. |
| **A04: Insecure Design** | Absence totale de numéros clients collectés côté front-end ; redirection exclusive via Checkout URL hébergée. |
| **A05: Security Misconfiguration** | Helmet (CSP, HSTS, X-Content-Type-Options, Frameguard) et CORS strict sans wildcard en production. |
| **A07: Identification and Auth Failures** | Protection anti-bruteforce et limitation de débit par IP et par compte utilisateur. |
| **A08: Software and Data Integrity** | Protection Anti-Replay empêchant le double traitement des ordres de paiement grâce aux transactions atomiques. |
| **A09: Security Logging & Monitoring** | Masquage des données sensibles dans les logs et codes d'erreur génériques vers le client. |

## 4. Démarrage Rapide

```bash
cd backend
npm install
npm run dev
```
