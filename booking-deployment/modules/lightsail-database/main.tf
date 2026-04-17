# ─── Auto-Generated Database Password ────

resource "random_password" "db" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# ─── Lightsail Managed PostgreSQL Database ─

resource "aws_lightsail_database" "postgres" {
  relational_database_name = "${var.project_name}-db"
  blueprint_id             = var.db_blueprint_id
  bundle_id                = var.db_bundle_id

  master_database_name = var.db_name
  master_username      = var.db_username
  master_password      = random_password.db.result

  publicly_accessible = var.db_publicly_accessible
  skip_final_snapshot = true
  apply_immediately   = true

  tags = {
    Name      = "${var.project_name}-db"
    ManagedBy = "terraform"
  }
}

# ─── Secrets Manager Secret ─────────────

resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "${var.project_name}/db-credentials"
  description = "Database credentials for ${var.project_name}"

  tags = {
    Name      = "${var.project_name}-db-credentials"
    ManagedBy = "terraform"
  }
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id

  secret_string = jsonencode({
    HOST      = aws_lightsail_database.postgres.master_endpoint_address
    PORT      = tostring(aws_lightsail_database.postgres.master_endpoint_port)
    DB_NAME   = aws_lightsail_database.postgres.master_database_name
    DB_SCHEMA = var.db_schema
    USER      = aws_lightsail_database.postgres.master_username
    PASSWORD  = random_password.db.result
  })
}
