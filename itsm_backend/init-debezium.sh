#!/bin/bash
echo "⏳ Waiting for Debezium to be ready..."
sleep 10

echo "📡 Checking existing connectors..."
EXISTS=$(curl -s http://connect:8083/connectors | grep ticket-postgres-connector)

if [ -z "$EXISTS" ]; then
  echo "🚀 Creating Debezium connector..."
  curl -X POST -H "Content-Type: application/json" --data @/postgres-connector.json http://connect:8083/connectors
else
  echo "✅ Connector already exists. Skipping creation."
fi
# Invoke-RestMethod -Uri http://localhost:8083/connectors -Method Post -Headers @{ "Content-Type" = "application/json" } -Body (Get-Content -Raw -Path ".\postgres-connector.json")
#Invoke-RestMethod -Uri http://localhost:8083/connectors

