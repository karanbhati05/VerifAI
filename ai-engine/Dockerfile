# Use a lightweight Python version
FROM python:3.10-slim

# 1. Install System Dependencies
# We need to install Tesseract and OpenGL (for OpenCV) inside the Linux container
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 2. Set the working directory
WORKDIR /app

# 3. Copy the requirements file and install Python libraries
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Copy the rest of the application code
COPY . .

# 5. Create the uploads folder inside the container
RUN mkdir -p uploads

# 6. Command to run the app
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5000"]