#!/bin/bash
set -euo pipefail

BRANCH=${1:-main}
PROJECT_DIR="/opt/istudyspot"
LOG_FILE="/opt/istudyspot/logs/deploy.log"
MAX_HEALTH_RETRIES=30
HEALTH_CHECK_INTERVAL=5

log() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[${timestamp}] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "Starting deployment for branch: ${BRANCH}"
log "=========================================="

cd "$PROJECT_DIR"

log "Step 1: Fetching latest code from remote..."
git fetch origin

log "Step 2: Checking out branch ${BRANCH}..."
git checkout "$BRANCH"

log "Step 3: Pulling latest changes..."
git pull origin "$BRANCH"

log "Step 4: Building backend Docker image..."
docker compose build backend --no-cache

log "Step 5: Stopping old backend container..."
docker compose stop backend

log "Step 6: Removing old backend container..."
docker compose rm -f backend

log "Step 7: Starting new backend container..."
docker compose up -d backend

log "Step 8: Waiting for backend to be healthy..."
retries=0
while [ $retries -lt $MAX_HEALTH_RETRIES ]; do
    if curl -sf http://localhost:8080/api/test > /dev/null 2>&1; then
        log "Backend is healthy!"
        break
    fi
    retries=$((retries + 1))
    log "Waiting for backend... (attempt ${retries}/${MAX_HEALTH_RETRIES})"
    sleep $HEALTH_CHECK_INTERVAL
done

if [ $retries -eq $MAX_HEALTH_RETRIES ]; then
    log "ERROR: Backend failed to start within timeout"
    log "Showing backend logs for debugging:"
    docker compose logs backend --tail 50
    exit 1
fi

log "Step 9: Cleaning up unused Docker resources..."
docker image prune -f

log "=========================================="
log "Deployment completed successfully!"
log "Branch: ${BRANCH}"
log "Time: $(date '+%Y-%m-%d %H:%M:%S')"
log "=========================================="