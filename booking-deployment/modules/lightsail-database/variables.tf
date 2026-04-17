variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "db_name" {
  description = "Name of the initial database"
  type        = string
}

variable "db_schema" {
  description = "Default schema name for the application"
  type        = string
}

variable "db_username" {
  description = "Master username for the database"
  type        = string
  default     = "root"
}

variable "db_bundle_id" {
  description = "Lightsail database bundle ID"
  type        = string
}

variable "db_blueprint_id" {
  description = "PostgreSQL engine version"
  type        = string
}

variable "db_publicly_accessible" {
  description = "Whether the database is publicly accessible"
  type        = bool
}
