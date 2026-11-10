import os
from pathlib import Path
from typing import Optional
import uuid
from fastapi import UploadFile

from app.core.config import BACKEND_DIR
from app.core.exceptions import ValidationError

STORAGE_DIR = BACKEND_DIR / "storage" / "complaints"
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp"}
ALLOWED_MIME_TYPES = {"image/png", "image/jpeg", "image/webp", "image/pjpeg"}
MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024  # 5 MB


def save_complaint_attachment(file: UploadFile) -> str:
    """
    Validate and securely save complaint photo attachment to durable disk storage.
    Files in this directory are never mounted or exposed via public static endpoints.
    Returns relative path.
    """
    STORAGE_DIR.mkdir(parents=True, exist_ok=True)

    filename = file.filename or "attachment.png"
    ext = os.path.splitext(filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise ValidationError(f"Invalid image extension '{ext}'. Allowed: {', '.join(sorted(ALLOWED_EXTENSIONS))}")

    if file.content_type and file.content_type.lower() not in ALLOWED_MIME_TYPES:
        raise ValidationError(f"Invalid file content-type '{file.content_type}'. Allowed: images (JPEG, PNG, WEBP)")

    content = file.file.read()
    if len(content) == 0:
        raise ValidationError("Uploaded complaint image is empty")
    if len(content) > MAX_FILE_SIZE_BYTES:
        raise ValidationError(f"Uploaded image exceeds maximum allowed size of {MAX_FILE_SIZE_BYTES // (1024 * 1024)} MB")

    unique_filename = f"complaint_{uuid.uuid4().hex[:16]}{ext}"
    destination_path = STORAGE_DIR / unique_filename

    with open(destination_path, "wb") as f:
        f.write(content)

    return f"storage/complaints/{unique_filename}"


def get_attachment_full_path(attachment_path: str) -> Path:
    """
    Safely resolve full path of complaint attachment, preventing directory traversal.
    """
    STORAGE_DIR.mkdir(parents=True, exist_ok=True)
    target = (BACKEND_DIR / attachment_path).resolve()
    storage_root = STORAGE_DIR.resolve()

    if not str(target).startswith(str(storage_root)):
        raise ValidationError("Invalid complaint attachment path")
    return target
