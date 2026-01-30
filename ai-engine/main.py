from fastapi import FastAPI, File, UploadFile, Form
from deepface import DeepFace
import cv2
import numpy as np
import os
import shutil
import pytesseract
from PIL import Image

# Tell Python where Tesseract is installed
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

app = FastAPI() # (This line is already in your code)

# Create a temp folder for processing
UPLOAD_DIR = "temp_uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.get("/")
def home():
    return {"status": "AI Engine is Online", "model": "Facenet512"}

@app.post("/verify")
async def verify_faces(
        id_card: UploadFile = File(...),
        selfie: UploadFile = File(...)
):
    try:
        # 1. Save files to disk
        id_path = os.path.join(UPLOAD_DIR, f"temp_{id_card.filename}")
        selfie_path = os.path.join(UPLOAD_DIR, f"temp_{selfie.filename}")

        with open(id_path, "wb") as buffer:
            shutil.copyfileobj(id_card.file, buffer)

        with open(selfie_path, "wb") as buffer:
            shutil.copyfileobj(selfie.file, buffer)

        # --- NEW STEP: OCR (Read Text) ---
        print("Extracting Text from ID Card...")
        extracted_text = pytesseract.image_to_string(Image.open(id_path))

        # Print it to the terminal so we can see if it works
        print("--- START OF TEXT ---")
        print(extracted_text)
        print("--- END OF TEXT ---")
        # ---------------------------------

        # 2. Run DeepFace (Existing Logic)
        print("Analyzing Faces...")
        result = DeepFace.verify(
            img1_path = id_path,
            img2_path = selfie_path,
            model_name = "Facenet512",
            detector_backend = "retinaface",
            distance_metric = "cosine"
        )

        distance = result['distance']
        CUSTOM_THRESHOLD = 0.70

        if distance < CUSTOM_THRESHOLD:
            match = True
        else:
            match = False

        confidence = round((1 - distance) * 100, 2)

        # 3. Clean up
        if os.path.exists(id_path): os.remove(id_path)
        if os.path.exists(selfie_path): os.remove(selfie_path)

        # Return the text along with the match result
        return {
            "match": match,
            "confidence": confidence / 100,
            "details": f"Distance: {distance}",
            "extracted_text": extracted_text  # <--- Sending text back to Java!
        }

    except Exception as e:
        print(f"Error: {e}")
        return {"match": False, "error": str(e)}

# To run: uvicorn main:app --reload --port 5000