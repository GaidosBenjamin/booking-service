variable "domain" {
  description = "Root domain managed in Cloudflare"
  type        = string
}

variable "subdomain" {
  description = "Subdomain for the CNAME record (e.g., 'api' for api.company.com)"
  type        = string
}

variable "container_service_url" {
  description = "Lightsail Container Service URL to point the CNAME at"
  type        = string
}

variable "proxied" {
  description = "Whether Cloudflare proxying is enabled"
  type        = bool
}
