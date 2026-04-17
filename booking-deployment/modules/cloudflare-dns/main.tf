# ─── Cloudflare Zone Lookup ──────────────

data "cloudflare_zone" "main" {
  name = var.domain
}

# ─── CNAME → Lightsail Container Service ─

resource "cloudflare_record" "app" {
  zone_id = data.cloudflare_zone.main.id
  name    = var.subdomain
  value   = replace(var.container_service_url, "https://", "")
  type    = "CNAME"
  ttl     = 1       # Auto TTL (required when proxied = true)
  proxied = var.proxied
}
