# ─── Container Registry ───────────────────

output "registry_url" {
  description = "Docker registry URL for this Lightsail Container Service — use as Jib <image> target"
  value       = module.lightsail-container.registry_url
}

output "jib_image" {
  description = "Full Jib <image> value: registry-url/container-name (without tag, Jib appends it)"
  value       = "${module.lightsail-container.registry_url}/booking-service"
}

# ─── Compute ─────────────────────────────

output "container_service_url" {
  description = "Public URL of the Lightsail Container Service"
  value       = module.lightsail-container.service_url
}

# ─── Database ────────────────────────────

output "db_endpoint" {
  description = "PostgreSQL connection endpoint"
  value       = module.lightsail-database.db_endpoint
}

output "db_port" {
  description = "PostgreSQL port"
  value       = module.lightsail-database.db_port
}

output "db_username" {
  description = "Database master username"
  value       = module.lightsail-database.db_username
}

output "db_secret_arn" {
  description = "ARN of the Secrets Manager secret with full DB credentials"
  value       = module.lightsail-database.secret_arn
}

output "db_secret_name" {
  description = "Name of the Secrets Manager secret"
  value       = module.lightsail-database.secret_name
}

# ─── Storage ─────────────────────────────

output "bucket_name" {
  description = "Lightsail bucket name for image storage"
  value       = module.lightsail-storage.bucket_name
}

output "bucket_access_key_id" {
  description = "Access key ID for the storage bucket"
  value       = module.lightsail-storage.bucket_access_key_id
  sensitive   = true
}

output "bucket_secret_access_key" {
  description = "Secret access key for the storage bucket"
  value       = module.lightsail-storage.bucket_secret_access_key
  sensitive   = true
}

# ─── DNS ─────────────────────────────────

output "app_fqdn" {
  description = "Fully qualified domain name for the application"
  value       = module.cloudflare-dns.fqdn
}

output "app_url" {
  description = "Public application URL"
  value       = "https://${module.cloudflare-dns.fqdn}"
}
