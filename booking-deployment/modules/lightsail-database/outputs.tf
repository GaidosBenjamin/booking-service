output "db_endpoint" {
  description = "PostgreSQL connection endpoint (host)"
  value       = aws_lightsail_database.postgres.master_endpoint_address
}

output "db_port" {
  description = "PostgreSQL port"
  value       = aws_lightsail_database.postgres.master_endpoint_port
}

output "db_name" {
  description = "Database name"
  value       = aws_lightsail_database.postgres.master_database_name
}

output "db_schema" {
  description = "Database Schema name"
  value       = var.db_schema
}

output "db_username" {
  description = "Database master username"
  value       = aws_lightsail_database.postgres.master_username
}

output "db_password" {
  description = "Database master password (auto-generated)"
  value       = random_password.db.result
  sensitive   = true
}

output "secret_arn" {
  description = "ARN of the Secrets Manager secret containing DB credentials"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

output "secret_name" {
  description = "Name of the Secrets Manager secret"
  value       = aws_secretsmanager_secret.db_credentials.name
}
