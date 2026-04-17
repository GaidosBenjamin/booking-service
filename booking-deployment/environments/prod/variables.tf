variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used as prefix for all resources"
  type        = string
  default     = "booking-service"
}

variable "container_image_tag" {
  description = "Docker image tag to deploy (e.g. 'latest' or a git commit SHA)"
  type        = string
  default     = "latest"
}