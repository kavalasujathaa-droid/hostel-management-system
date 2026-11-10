import logging
import re
import sys
from typing import Any
from app.core.config import settings

# Sensitive patterns to sanitize from logs
SENSITIVE_PATTERNS = [
    re.compile(r"(password=)[^&\s]+", re.IGNORECASE),
    re.compile(r"(pwd=)[^&\s]+", re.IGNORECASE),
    re.compile(r"(bearer\s+)[a-zA-Z0-9_\-\.]+", re.IGNORECASE),
    re.compile(r"(authorization:\s*)[^\s]+", re.IGNORECASE),
    re.compile(r"(mysql\+pymysql://[^:]+:)[^@]+(@)", re.IGNORECASE),
]


class SensitiveDataFilter(logging.Filter):
    """Filter to redact sensitive patterns from log records."""

    def filter(self, record: logging.LogRecord) -> bool:
        if isinstance(record.msg, str):
            for pattern in SENSITIVE_PATTERNS:
                if "mysql+pymysql" in pattern.pattern:
                    record.msg = pattern.sub(r"\1***\2", record.msg)
                else:
                    record.msg = pattern.sub(r"\1***", record.msg)

        if record.args:
            sanitized_args = []
            for arg in record.args if isinstance(record.args, tuple) else [record.args]:
                if isinstance(arg, str):
                    for pattern in SENSITIVE_PATTERNS:
                        if "mysql+pymysql" in pattern.pattern:
                            arg = pattern.sub(r"\1***\2", arg)
                        else:
                            arg = pattern.sub(r"\1***", arg)
                sanitized_args.append(arg)
            record.args = tuple(sanitized_args) if isinstance(record.args, tuple) else sanitized_args[0]

        return True


def setup_logging() -> logging.Logger:
    """Configure centralized application logging."""
    log_level = logging.DEBUG if settings.DEBUG else logging.INFO
    log_format = "%(asctime)s | %(levelname)-8s | %(name)s:%(funcName)s:%(lineno)d - %(message)s"

    handler = logging.StreamHandler(sys.stdout)
    handler.setLevel(log_level)
    handler.setFormatter(logging.Formatter(log_format))
    handler.addFilter(SensitiveDataFilter())

    root_logger = logging.getLogger()
    # Avoid duplicate handlers if setup_logging is called multiple times
    if not any(isinstance(h, logging.StreamHandler) for h in root_logger.handlers):
        root_logger.addHandler(handler)
    root_logger.setLevel(log_level)

    app_logger = logging.getLogger("hostel_app")
    app_logger.setLevel(log_level)

    # Silence overly verbose third-party loggers in dev/prod
    logging.getLogger("urllib3").setLevel(logging.WARNING)
    logging.getLogger("passlib").setLevel(logging.WARNING)

    return app_logger


logger = setup_logging()
