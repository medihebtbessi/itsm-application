#!/bin/bash
echo "⏳ Waiting for Debezium to be ready..."
sleep 10

echo "📡 Checking existing connectors..."
EXISTS=$(curl -s http://connect:8083/connectors | grep ticket-postgres-connector)

if [ -z "$EXISTS" ]; then
  echo "🚀 Creating Debezium connector..."
  curl -X POST -H "Content-Type: application/json" --data @/kafka/postgres-connector.json http://connect:8083/connectors
else
  echo "✅ Connector already exists. Skipping creation."
fi
