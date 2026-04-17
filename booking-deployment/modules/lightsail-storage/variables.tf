variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "bucket_bundle_id" {
  description = "Lightsail bucket bundle ID"
  type        = string
  default     = "small_1_0"
}

variable "container_service_name" {
  description = "Name of the Lightsail container service (for bucket resource access)"
  type        = string
}
