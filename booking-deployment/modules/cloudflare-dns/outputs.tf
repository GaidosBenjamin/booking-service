output "fqdn" {
  description = "Fully qualified domain name of the DNS record"
  value       = cloudflare_record.app.hostname
}
