# ─── Lightsail Container Service (w/ built-in registry) ─────

resource "aws_lightsail_container_service" "this" {
  name        = var.service_name
  power       = var.power
  scale       = var.scale
  is_disabled = false

  tags = {
    Name      = var.service_name
    ManagedBy = "terraform"
  }
}
