#!/bin/bash
# =============================================================================
# LocalStack Initialization Script
# Runs automatically when LocalStack becomes ready (mounted into ready.d/).
# Creates all AWS resources needed by the Spring Boot application.
# =============================================================================

set -e

echo "=========================================="
echo "Initializing LocalStack AWS resources..."
echo "=========================================="

# --- S3 Bucket ---
echo "Creating S3 bucket: poc-bucket"
awslocal s3 mb s3://poc-bucket 2>/dev/null || echo "  -> Bucket already exists"

# --- SQS Queue ---
echo "Creating SQS queue: poc-queue"
awslocal sqs create-queue --queue-name poc-queue 2>/dev/null || echo "  -> Queue already exists"

# --- DynamoDB Table ---
# Single partition key (id), on-demand billing (no capacity planning needed)
echo "Creating DynamoDB table: poc-table"
awslocal dynamodb create-table \
    --table-name poc-table \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    2>/dev/null || echo "  -> Table already exists"

# --- SNS Topic ---
echo "Creating SNS topic: poc-topic"
awslocal sns create-topic --name poc-topic 2>/dev/null || echo "  -> Topic already exists"

# --- Verification ---
echo ""
echo "=========================================="
echo "Resource verification:"
echo "=========================================="
echo "S3 Buckets:"
awslocal s3 ls
echo ""
echo "SQS Queues:"
awslocal sqs list-queues
echo ""
echo "DynamoDB Tables:"
awslocal dynamodb list-tables
echo ""
echo "SNS Topics:"
awslocal sns list-topics
echo ""
echo "=========================================="
echo "LocalStack initialization complete!"
echo "=========================================="
