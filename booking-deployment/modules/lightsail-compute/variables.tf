variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "container_image" {
  description = "Docker image for the Spring Boot application')"
  type        = string
}

variable "container_service_name" {
  description = "Lightsail container service name"
  type        = string
}

variable "container_port" {
  description = "Port the Spring Boot application listens on"
  type        = number
}

variable "environment" {
  description = "Environment variables to pass to the container"
  type        = map(string)
  default     = {}
}
