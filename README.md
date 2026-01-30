# 🛡️ VerifAI: Distributed e-KYC System

> A full-stack identity verification system leveraging **Distributed Architecture**, **Deep Learning (Facenet512)**, and **OCR** to perform real-time KYC (Know Your Customer) validation.

## 🚀 Key Features
- **Hybrid Verification Pipeline**: Combines **DeepFace (Computer Vision)** for facial biometrics and **Tesseract OCR** for text extraction.
- **Distributed Architecture**: Decoupled **Spring Boot (Orchestrator)** and **FastAPI (AI Engine)** services for scalability.
- **Live Liveness Detection**: Supports real-time webcam capture and file uploads.
- **Bank-Grade Accuracy**: Uses **RetinaFace** for detection and **Facenet512** for embeddings (99.4% LFW accuracy).
- **Persistent Audit Trail**: Logs all verification attempts, confidence scores, and extracted data in **MySQL**.

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend Orchestrator** | Java (Spring Boot) | Handles API requests, file management, and database transactions. |
| **AI Engine** | Python (FastAPI) | Runs the heavy ML models (DeepFace, Pytesseract). |
| **Database** | MySQL | Stores verification logs, user metadata, and status reports. |
| **Frontend** | HTML5 / JS | Asynchronous UI for image capture and real-time status updates. |
| **Containerization** | Docker | Full system orchestration via `docker-compose`. |

---

## ⚙️ System Architecture
1. **Client** uploads ID + Selfie via Frontend.
2. **Spring Boot** receives files and creates a transaction record.
3. **Spring Boot** asynchronously sends files to the **Python AI Microservice**.
4. **Python Engine**:
    - Detects faces using **RetinaFace**.
    - Generates 512D embeddings using **Facenet512**.
    - Calculates Cosine Similarity.
    - Extracts ID card text using **LSTM-based OCR**.
5. **Result** (Approved/Rejected + Data) is returned to Java and saved to MySQL.
6. **Frontend** polls for the final verdict.

---

## 🔌 How to Run (Docker)
The entire system can be spun up with a single command:

```bash
docker-compose up --build