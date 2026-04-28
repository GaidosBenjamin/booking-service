variable "project_name" {
  description = "Project name used as a prefix for resource naming"
  type        = string
}

variable "domain" {
  description = "Root domain managed in Cloudflare"
  type        = string
}

variable "subdomain" {
  description = "Subdomain for the SPA (e.g., 'camp' for camp.bbso.dev)"
  type        = string
}

variable "index_document" {
  description = "Default root object served by CloudFront"
  type        = string
  default     = "index.html"
}

variable "certificate_arn" {
  description = "ARN of an ACM certificate in us-east-1 to attach to the CloudFront distribution"
  type        = string
}

variable "price_class" {
  description = "CloudFront price class (controls edge geography)"
  type        = string
  default     = "PriceClass_100"
}
