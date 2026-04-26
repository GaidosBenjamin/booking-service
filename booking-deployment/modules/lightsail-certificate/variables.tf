variable "certificate_name" {
  description = "Unique name for the Lightsail certificate"
  type        = string
}

variable "domain" {
  description = "Root domain managed in Cloudflare (e.g. bbso.dev)"
  type        = string
}

variable "subdomain" {
  description = "Subdomain to issue the certificate for (e.g. api)"
  type        = string
}

variable "aws_region" {
  description = "AWS region used by the AWS CLI when polling for certificate validation"
  type        = string
}
