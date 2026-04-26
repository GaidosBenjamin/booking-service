variable "service_name" {
  description = "Name of the Lightsail Container Service (also the registry hostname prefix)"
  type        = string
}

variable "power" {
  description = "Lightsail container service power (nano, micro, small, medium, large, xlarge)"
  type        = string
  default     = "nano"
}

variable "scale" {
  description = "Number of compute nodes to run"
  type        = number
  default     = 1
}

variable "aws_region" {
  description = "AWS region where the service is deployed (used to construct the registry URL)"
  type        = string
  default     = "us-east-1"
}

variable "certificate_name" {
  description = "Lightsail certificate name to attach for a custom domain (null disables custom domain)"
  type        = string
  default     = null
}

variable "custom_domain" {
  description = "Custom domain to associate with the service (e.g. api.bbso.dev)"
  type        = string
  default     = null
}
