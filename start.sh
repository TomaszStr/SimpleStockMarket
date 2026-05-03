#!/bin/bash

# Check if port parameter is provided
if [ -z "$1" ]; then
  echo "Usage: ./start.sh <PORT>"
  echo "Example: ./start.sh 8080"
  exit 1
fi

export APP_PORT=$1
echo "Starting Simple Stock Market on localhost:$APP_PORT..."

# Build and start in detached mode
docker-compose up -d --build

echo "Application is spinning up. Give it a few seconds to initialize the database and replicas."