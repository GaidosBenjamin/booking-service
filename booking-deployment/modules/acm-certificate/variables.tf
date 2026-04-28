variable "domain" {
  description = "Root domain — issues a wildcard certificate for *.{domain} (e.g. 'bbso.dev' → '*.bbso.dev')"
  type        = string
}
