# ─── Lightsail Certificate ───────────────

resource "aws_lightsail_certificate" "this" {
  name        = var.certificate_name
  domain_name = "${var.subdomain}.${var.domain}"
}

# ─── Cloudflare Zone Lookup ──────────────

data "cloudflare_zone" "main" {
  name = var.domain
}

# ─── DNS Validation Records ──────────────

locals {
  fqdn = "${var.subdomain}.${var.domain}"

  validation_options = {
    for dvo in aws_lightsail_certificate.this.domain_validation_options : dvo.domain_name => dvo
  }
}

resource "cloudflare_record" "validation" {
  for_each = toset([local.fqdn])

  zone_id = data.cloudflare_zone.main.id
  name    = replace(local.validation_options[each.key].resource_record_name, ".${var.domain}.", "")
  content = trimsuffix(local.validation_options[each.key].resource_record_value, ".")
  type    = local.validation_options[each.key].resource_record_type
  ttl     = 60
  proxied = false
}

# ─── Wait for Certificate Validation ─────
# Lightsail has no aws_lightsail_certificate_validation resource (unlike ACM),
# so poll the cert status via AWS CLI until it reaches ISSUED before any
# downstream resource attempts to attach it.

resource "null_resource" "wait_for_validation" {
  depends_on = [cloudflare_record.validation]

  triggers = {
    certificate_arn = aws_lightsail_certificate.this.arn
  }

  provisioner "local-exec" {
    interpreter = ["/bin/bash", "-c"]
    command     = <<-EOT
      set -e
      for i in $(seq 1 60); do
        STATUS=$(aws lightsail get-certificates \
          --region ${var.aws_region} \
          --certificate-name ${var.certificate_name} \
          --query 'certificates[0].certificateDetail.status' \
          --output text 2>/dev/null || echo "UNKNOWN")
        echo "[$i/60] Certificate '${var.certificate_name}' status: $STATUS"
        if [ "$STATUS" = "ISSUED" ]; then
          exit 0
        fi
        sleep 10
      done
      echo "Timed out waiting for certificate to reach ISSUED status"
      exit 1
    EOT
  }
}
