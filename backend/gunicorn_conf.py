import multiprocessing
import os

# Server socket
host = os.getenv("HOST", "0.0.0.0")
port = os.getenv("PORT", "8000")
bind = f"{host}:{port}"
backlog = 2048

# Worker processes
workers = int(os.getenv("WEB_CONCURRENCY", max(2, min(4, multiprocessing.cpu_count()))))
worker_class = "uvicorn.workers.UvicornWorker"
worker_connections = 1000
timeout = int(os.getenv("GUNICORN_TIMEOUT", "60"))
keepalive = 5
graceful_timeout = 30

# Memory leak and worker restart prevention
max_requests = 1000
max_requests_jitter = 50

# Logging
accesslog = "-"
errorlog = "-"
loglevel = os.getenv("LOG_LEVEL", "info").lower()
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s" (%(L)ss)'

# Process naming
proc_name = "hostel_management_api"
