# Infrastructure

The current demo is containerized with Docker Compose so the complete service graph can be started locally with one command.

A production-oriented next milestone is to add Terraform for:

- ECS/Fargate services for backend and ML
- RDS PostgreSQL
- Application Load Balancer
- CloudWatch/OpenTelemetry collection
- Secrets Manager for credentials
- VPC/security groups

Do not claim Terraform/AWS deployment for RentalIQ until that infrastructure is actually implemented and deployed.
