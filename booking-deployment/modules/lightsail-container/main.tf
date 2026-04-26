# ─── Lightsail Container Service (w/ built-in registry) ─────

resource "aws_lightsail_container_service" "this" {
  name        = var.service_name
  power       = var.power
  scale       = var.scale
  is_disabled = false

  dynamic "public_domain_names" {
    for_each = var.certificate_name != null ? [1] : []
    content {
      certificate {
        certificate_name = var.certificate_name
        domain_names     = [var.custom_domain]
      }
    }
  }

  tags = {
    Name      = var.service_name
    ManagedBy = "terraform"
  }
}
