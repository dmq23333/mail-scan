#!/usr/bin/env bash
set -euo pipefail

# Start script kept in a separate file so the runtime command is isolated and easy to change.
# It honours environment variables (e.g. to set JVM options).

JVM_OPTS="${JVM_OPTS:--Xms256m -Xmx512m}"
APP_JAR="/app/app.jar"

echo "Starting job-mail-scan with: java $JVM_OPTS -jar $APP_JAR"
exec java $JVM_OPTS -jar $APP_JAR
