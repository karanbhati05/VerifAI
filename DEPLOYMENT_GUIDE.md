# 🚀 VerifAI 100% Free Tier Deployment Guide ($0.00 Cost)

This guide is designed for a **100% free demo**. **Zero credit cards, zero paid plans, zero hidden fees.**

---

## 💰 Free Tier Architecture Matrix

| Service | Platform | Cost | Credit Card? | Free Limits |
|---|---|---|---|---|
| **Frontend** | **Vercel** | **$0** | **NO** | 100 GB bandwidth, unlimited static hosting |
| **Backend Orchestrator** | **Render** | **$0** | **NO** | 750 free instance hours / month |
| **AI Microservice** | **Render** | **$0** | **NO** | 512 MB RAM, 0.1 CPU |
| **Database** | **Built-in H2** OR **TiDB Cloud** | **$0** | **NO** | In-Memory (Zero accounts) or 5GB Serverless |

---

## 📦 Step 0: Push Prepared Code to GitHub

All free-tier optimizations have been applied:
- **JVM memory guardrails**: Capped at 256MB heap so Spring Boot won't exceed Render's 512MB limit.
- **Fast Face Detection**: Uses lightweight `opencv` detector by default so the Python container uses under 100MB RAM instead of crashing on 512MB.
- **H2 In-Memory DB Support**: Allows running without creating ANY external database account if you prefer!

Run in your terminal:
```bash
git add .
git commit -m "feat: configure 100% free tier deployment for Render and Vercel"
git push origin main
```

---

## 🗄️ Step 1: Choose Your Free Database Option

You have two 100% free options:

### 🌟 Option A: Built-in In-Memory Database (Easiest, ZERO Signups, $0)
If you don't want to sign up for any database provider, Spring Boot now has **H2 database built-in**.
- When deploying the backend in Step 3, simply use these environment variables:
  ```
  SPRING_DATASOURCE_URL = jdbc:h2:mem:verifai_db;DB_CLOSE_DELAY=-1
  SPRING_DATASOURCE_USERNAME = sa
  SPRING_DATASOURCE_PASSWORD = password
  ```
- *No extra accounts needed, works instantly.*

### Option B: TiDB Cloud Serverless (Persistent MySQL, 100% Free Forever, $0)
If you want verification records to persist permanently across restarts:
1. Go to [TiDB Cloud](https://tidbcloud.com/) and register for free (No credit card needed).
2. Click **Create Cluster** > Select **Serverless (Free)**.
3. In **Security** / **IP Access**, allow `0.0.0.0/0`.
4. Copy the connection parameters (Host, Port, User, Password).
5. Your JDBC URL will be:
   ```
   jdbc:mysql://<HOST>:<PORT>/test?useSSL=true&serverTimezone=UTC
   ```

---

## 🧠 Step 2: Deploy AI Engine (Python FastAPI) on Render ($0)

1. Go to [Render Dashboard](https://dashboard.render.com/) and sign up with your GitHub account (No credit card needed).
2. Click **New +** > **Web Service**.
3. Select your repository: `karanbhati05/VerifAI-KYC-System`.
4. Configure:
   - **Name**: `verifai-ai-engine`
   - **Region**: Choose Singapore, Frankfurt, or Oregon (pick whichever is closest to you).
   - **Root Directory**: `ai-engine`
   - **Runtime**: **Docker**
   - **Instance Type**: **Free** (512 MB)
5. Under **Environment Variables**, add:
   - `PYTHONUNBUFFERED`: `1`
   - `DETECTOR_BACKEND`: `opencv` *(keeps memory under 100MB for the 512MB free tier)*
6. Click **Deploy Web Service**.
7. Once deployed (~3-5 mins), test in browser:
   ```
   https://verifai-ai-engine.onrender.com/
   ```
   Output: `{"status":"AI Engine is Online","model":"Facenet512"}`.
8. **Copy URL**: `https://verifai-ai-engine.onrender.com/verify`.

---

## ☕ Step 3: Deploy Backend Orchestrator (Spring Boot) on Render ($0)

1. In Render Dashboard, click **New +** > **Web Service**.
2. Select your repository again.
3. Configure:
   - **Name**: `verifai-backend`
   - **Region**: Same region as AI Engine.
   - **Root Directory**: `verifai-backend`
   - **Runtime**: **Docker**
   - **Instance Type**: **Free**
4. Under **Environment Variables**, add:

   | Key | Value (Option A - Zero Setup H2) | Value (Option B - TiDB Cloud) |
   |---|---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:verifai_db;DB_CLOSE_DELAY=-1` | `jdbc:mysql://<HOST>:<PORT>/test?useSSL=true&serverTimezone=UTC` |
   | `SPRING_DATASOURCE_USERNAME` | `sa` | `<DB_USER>` |
   | `SPRING_DATASOURCE_PASSWORD` | `password` | `<DB_PASSWORD>` |
   | `AI_ENGINE_URL` | `https://verifai-ai-engine.onrender.com/verify` | `https://verifai-ai-engine.onrender.com/verify` |

5. Click **Deploy Web Service**.
6. When live, test the health check in your browser:
   ```
   https://verifai-backend.onrender.com/api/health
   ```
   Output: `"VerifAI Backend is Running! Database Connection: Stable."`
7. **Copy URL**: `https://verifai-backend.onrender.com`.

---

## 🌐 Step 4: Deploy Frontend on Vercel ($0)

1. Go to [Vercel](https://vercel.com/) and log in with your GitHub account (100% Free Hobby Tier, No credit card needed).
2. Click **Add New...** > **Project** and import your repository.
3. In project settings:
   - **Framework Preset**: **Other**
   - **Root Directory**: Click *Edit* and select **`VerifAI-frontend`**.
4. Click **Deploy**.
5. Once deployed, open your live Vercel domain (e.g. `https://verifai-frontend.vercel.app`):
   - Click the **⚙️ Change** button in the header.
   - Paste your Render backend URL: `https://verifai-backend.onrender.com`.
   - Click **Save & Connect**.
   - The indicator will turn **🟢 Backend: Ready**.

---

## 💡 Demo Day Tips for Free Tier

- **Render Free Tier Spin-Down**: Free tier services sleep if inactive for 15 minutes.
- **Before presenting your demo**: Open `https://verifai-backend.onrender.com/api/health` and `https://verifai-ai-engine.onrender.com/` in your browser 1 minute before your presentation. This wakes them up so your live demo is instant and smooth!

---

## 🧪 Automated Testing

Automated testing is configured for both the Spring Boot backend and the FastAPI AI engine:

1. **Local Test Runner**:
   ```bash
   python test_pipeline.py
   ```
2. **Spring Boot Tests only**:
   ```bash
   cd verifai-backend
   ./mvnw test
   ```
3. **Python AI Engine Tests only**:
   ```bash
   cd ai-engine
   pytest tests/ -v
   ```
4. **CI/CD Integration**:
   - Every `git push` automatically runs tests on GitHub Actions (`.github/workflows/test.yml`).

