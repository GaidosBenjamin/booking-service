variable "github_org" {
  description = "GitHub organisation or username that owns the repository"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name (without the org prefix)"
  type        = string
}

variable "create_oidc_provider" {
  description = "Set to false if a GitHub OIDC provider already exists in this account"
  type        = bool
  default     = true
}
