variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "bucket_bundle_id" {
  description = "Lightsail bucket bundle ID"
  type        = string
  default     = "small_1_0"
}
