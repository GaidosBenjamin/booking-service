# ─── Cloudflare Zone Lookup ──────────────

data "cloudflare_zone" "main" {
  name = var.domain
}

# ─── CNAME → Lightsail Container Service ─

resource "cloudflare_record" "app" {
  zone_id = data.cloudflare_zone.main.id
  name    = var.subdomain
  content = trimsuffix(replace(var.container_service_url, "https://", ""), "/")
  type    = "CNAME"
  ttl     = var.proxied ? 1 : 300
  proxied = var.proxied
}
