import pytest
from unittest.mock import patch, MagicMock
from fastapi.testclient import TestClient
import io
import sys
import os

# Ensure ai-engine root is in python path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from main import app

client = TestClient(app)

def test_health_home_endpoint():
    """Test the root health check endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "AI Engine is Online"
    assert "model" in data

def test_verify_endpoint_missing_files():
    """Test verification endpoint validation when files are missing"""
    response = client.post("/verify")
    # FastAPI returns 422 for missing required form/file fields
    assert response.status_code == 422

@patch("main.pytesseract.image_to_string")
@patch("main.DeepFace.verify")
def test_verify_endpoint_matched(mock_deepface, mock_ocr):
    """Test verification endpoint with matching face and extracted OCR"""
    mock_ocr.return_value = "REPUBLIC OF INDIA\nName: Karan Bhati\nDOB: 01/01/2000"
    mock_deepface.return_value = {
        "distance": 0.30
    }

    dummy_id = io.BytesIO(b"fake_id_image_content")
    dummy_selfie = io.BytesIO(b"fake_selfie_image_content")

    response = client.post(
        "/verify",
        files={
            "id_card": ("id.jpg", dummy_id, "image/jpeg"),
            "selfie": ("selfie.jpg", dummy_selfie, "image/jpeg")
        }
    )

    assert response.status_code == 200
    data = response.json()
    assert data["match"] is True
    assert data["confidence"] == 0.70
    assert "Karan Bhati" in data["extracted_text"]

@patch("main.pytesseract.image_to_string")
@patch("main.DeepFace.verify")
def test_verify_endpoint_mismatch(mock_deepface, mock_ocr):
    """Test verification endpoint when distance exceeds threshold (face mismatch)"""
    mock_ocr.return_value = "Card Holder: John Doe"
    mock_deepface.return_value = {
        "distance": 0.85
    }

    dummy_id = io.BytesIO(b"fake_id_bytes")
    dummy_selfie = io.BytesIO(b"fake_selfie_bytes")

    response = client.post(
        "/verify",
        files={
            "id_card": ("id.jpg", dummy_id, "image/jpeg"),
            "selfie": ("selfie.jpg", dummy_selfie, "image/jpeg")
        }
    )

    assert response.status_code == 200
    data = response.json()
    assert data["match"] is False
    assert data["confidence"] == 0.15
