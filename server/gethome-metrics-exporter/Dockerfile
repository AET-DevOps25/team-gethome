FROM python:3.11-slim

LABEL maintainer="GetHome Team"
LABEL description="Custom Prometheus exporter for GetHome business intelligence"

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app/ ./app/

# Create non-root user for security
RUN groupadd -r gethome && useradd -r -g gethome gethome
RUN chown -R gethome:gethome /app
USER gethome

# Expose metrics port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8090/metrics', timeout=5)"

# Run the exporter
CMD ["python", "app/main.py"] 