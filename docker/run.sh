#!/usr/bin/env bash
set -euo pipefail

# Helper script to build and run the application with docker-compose.
# Usage: ./docker/run.sh [up|down|logs]

CMD=${1:-up}

COMPOSE_FILE="docker-compose.yml"

case "$CMD" in
  up)
    docker compose -f "$COMPOSE_FILE" up --build -d
    ;;
  down)
    docker compose -f "$COMPOSE_FILE" down
    ;;
  logs)
    docker compose -f "$COMPOSE_FILE" logs -f
    ;;
  *)
    echo "Usage: $0 [up|down|logs]"
    exit 1
    ;;
esac

