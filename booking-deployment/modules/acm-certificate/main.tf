# ─── ACM Wildcard Certificate (us-east-1, required by CloudFront) ─

resource "aws_acm_certificate" "wildcard" {
  provider          = aws.us_east_1
  domain_name       = "*.${var.domain}"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name      = "wildcard.${var.domain}"
    ManagedBy = "terraform"
  }
}

# ─── Cloudflare Zone Lookup ──────────────

data "cloudflare_zone" "main" {
  name = var.domain
}

# ─── DNS Validation Records ──────────────

resource "cloudflare_record" "validation" {
  for_each = {
    for dvo in aws_acm_certificate.wildcard.domain_validation_options : dvo.domain_name => dvo
  }

  zone_id = data.cloudflare_zone.main.id
  name    = replace(each.value.resource_record_name, ".${var.domain}.", "")
  content = trimsuffix(each.value.resource_record_value, ".")
  type    = each.value.resource_record_type
  ttl     = 60
  proxied = false
}

# ─── Wait for Certificate Validation ─────

resource "aws_acm_certificate_validation" "wildcard" {
  provider                = aws.us_east_1
  certificate_arn         = aws_acm_certificate.wildcard.arn
  validation_record_fqdns = [for r in cloudflare_record.validation : r.hostname]
}
