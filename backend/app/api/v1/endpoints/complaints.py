import mimetypes
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, Request, UploadFile, status
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.core.exceptions import ForbiddenError, NotFoundError, ValidationError
from app.db.session import get_db
from app.models.models import Complaint, TenantStay, User
from app.schemas.complaint import ComplaintCreate, ComplaintResponse, ComplaintUpdate
from app.services import complaint_attachment_service, complaint_service

router = APIRouter()


@router.post("", response_model=ComplaintResponse, status_code=status.HTTP_201_CREATED)
async def create_complaint(
    request: Request,
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    """
    Submit a complaint against tenant's active stay with title, category, and optional attachment.
    Supports both application/json and multipart/form-data.
    """
    content_type = request.headers.get("content-type", "")
    photo_file: Optional[UploadFile] = None

    if "multipart/form-data" in content_type:
        form = await request.form()
        try:
            raw_title = form.get("title") or "General Complaint"
            raw_category = form.get("category", "Other")
            raw_description = form.get("description")
            raw_stay_id = form.get("tenant_stay_id")

            if not raw_description:
                raise ValidationError("description is a required field")

            stay_id = int(str(raw_stay_id)) if raw_stay_id else None
            req = ComplaintCreate(
                title=str(raw_title).strip(),
                category=str(raw_category).strip(),
                description=str(raw_description).strip(),
                tenant_stay_id=stay_id,
            )

            raw_file = form.get("attachment") or form.get("photo_file")
            if raw_file and hasattr(raw_file, "filename") and getattr(raw_file, "filename"):
                photo_file = raw_file
        except (ValueError, TypeError) as exc:
            raise ValidationError(f"Invalid form data: {str(exc)}")
    else:
        body = await request.json()
        req = ComplaintCreate(**body)

    return complaint_service.create_complaint(
        db=db,
        req=req,
        tenant_user_id=current_user.id,
        photo_file=photo_file,
    )


@router.get("", response_model=List[ComplaintResponse], status_code=status.HTTP_200_OK)
def list_complaints(
    status: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[ComplaintResponse]:
    """List complaints. Tenants only see their own complaints."""
    tenant_id = current_user.id if current_user.role == "TENANT" else None
    return complaint_service.list_complaints(db, tenant_user_id=tenant_id, status=status)


@router.get("/{complaint_id}", response_model=ComplaintResponse, status_code=status.HTTP_200_OK)
def get_complaint(
    complaint_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    complaint = complaint_service.get_complaint_by_id(db, complaint_id)
    if current_user.role == "TENANT":
        user_complaints = complaint_service.list_complaints(db, tenant_user_id=current_user.id)
        if not any(c.id == complaint.id for c in user_complaints):
            raise ForbiddenError("Access forbidden to this complaint")
    return complaint


@router.get("/{complaint_id}/attachment", status_code=status.HTTP_200_OK)
def get_complaint_attachment(
    complaint_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    Secure authenticated retrieval of complaint photo attachment.
    Accessible ONLY by the owner or the submitting tenant.
    Never exposed to unauthenticated users or unrelated tenants.
    """
    complaint = db.query(Complaint).filter(Complaint.id == complaint_id).first()
    if not complaint or not complaint.attachment_path:
        raise NotFoundError("Complaint attachment not found")

    if current_user.role != "OWNER":
        stay = db.query(TenantStay).filter(TenantStay.id == complaint.tenant_stay_id).first()
        if not stay or stay.tenant_user_id != current_user.id:
            raise ForbiddenError("You are not authorized to view this complaint attachment")

    full_path = complaint_attachment_service.get_attachment_full_path(complaint.attachment_path)
    if not full_path.is_file():
        raise NotFoundError("Complaint attachment file not found in storage")

    content_type, _ = mimetypes.guess_type(str(full_path))
    media_type = content_type or "application/octet-stream"

    return FileResponse(
        path=str(full_path),
        media_type=media_type,
        filename=full_path.name,
    )


@router.put("/{complaint_id}", response_model=ComplaintResponse, status_code=status.HTTP_200_OK)
def update_complaint_status(
    complaint_id: int,
    req: ComplaintUpdate,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    """Update complaint status (OPEN, IN PROGRESS, RESOLVED)."""
    return complaint_service.update_complaint_status(db, complaint_id, req, owner_user_id=current_user.id)
