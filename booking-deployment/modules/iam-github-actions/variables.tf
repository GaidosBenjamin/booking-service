variable "github_org" {
  description = "GitHub organisation or username that owns the repository"
  type        = string
}

variable "github_repos" {
  description = "GitHub repository names (without the org prefix) that may assume this role"
  type        = list(string)
}

variable "create_oidc_provider" {
  description = "Set to false if a GitHub OIDC provider already exists in this account"
  type        = bool
  default     = true
}
