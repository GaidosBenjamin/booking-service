variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Project name used as prefix for all resources"
  type        = string
  default     = "booking-service"
}

variable "container_image_tag" {
  description = "Docker image tag to deploy (e.g. 'latest' or a git commit SHA)"
  type        = string
}

variable "jwt_secret" {
  description = "Secret key used to sign JWTs"
  type        = string
  sensitive   = true
}

variable "smtp_user" {
  description = "Brevo SMTP username"
  type        = string
}

variable "smtp_password" {
  description = "Brevo SMTP password"
  type        = string
  sensitive   = true
}

variable "cors_allowed_origins" {
  description = "Comma-separated list of allowed CORS origins"
  type        = string
}

variable "stripe_api_key" {
  description = "Stripe secret API key"
  type        = string
  sensitive   = true
}

variable "stripe_webhook_secret" {
  description = "Stripe webhook signing secret"
  type        = string
  sensitive   = true
}

variable "stripe_success_url" {
  description = "URL to redirect to after successful Stripe payment"
  type        = string
}

variable "stripe_cancel_url" {
  description = "URL to redirect to after cancelled Stripe payment"
  type        = string
}

variable "stripe_donation_success_url" {
  description = "URL to redirect to after successful Stripe donation"
  type        = string
}

variable "stripe_donation_cancel_url" {
  description = "URL to redirect to after cancelled Stripe donation"
  type        = string
}

variable "mail_from" {
  description = "From address used in outbound emails"
  type        = string
  default     = "no-reply@bbso.dev"
}

variable "mail_brand" {
  description = "Brand name shown in outbound emails"
  type        = string
  default     = "BBSO Kids"
}

variable "log_level" {
  description = "Log level for com.bgaidos packages"
  type        = string
  default     = "INFO"
}

variable "github_org" {
  description = "GitHub organisation or username that owns the repository"
  type        = string
}