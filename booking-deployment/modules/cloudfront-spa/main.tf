locals {
  fqdn        = "${var.subdomain}.${var.domain}"
  bucket_name = "bbso-${var.project_name}-${var.subdomain}-frontend"
  origin_id   = "s3-${local.bucket_name}"
}

# ─── Cloudflare Zone Lookup ──────────────

data "cloudflare_zone" "main" {
  name = var.domain
}

# ─── S3 Bucket (SPA Origin) ──────────────

resource "aws_s3_bucket" "spa" {
  bucket = local.bucket_name

  tags = {
    Name      = "${var.project_name}-${var.subdomain}-frontend"
    ManagedBy = "terraform"
  }
}

resource "aws_s3_bucket_versioning" "spa" {
  bucket = aws_s3_bucket.spa.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "spa" {
  bucket = aws_s3_bucket.spa.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "spa" {
  bucket = aws_s3_bucket.spa.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ─── CloudFront Origin Access Control ────

resource "aws_cloudfront_origin_access_control" "spa" {
  name                              = "${var.project_name}-${var.subdomain}-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# ─── CloudFront Distribution ─────────────

resource "aws_cloudfront_distribution" "spa" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = var.index_document
  price_class         = var.price_class
  aliases             = [local.fqdn]

  origin {
    domain_name              = aws_s3_bucket.spa.bucket_regional_domain_name
    origin_id                = local.origin_id
    origin_access_control_id = aws_cloudfront_origin_access_control.spa.id
  }

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = local.origin_id
    viewer_protocol_policy = "redirect-to-https"
    compress               = true
    # AWS-managed CachingOptimized policy
    cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6"
  }

  # SPA: serve index.html for client-side routes
  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/${var.index_document}"
  }

  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/${var.index_document}"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = var.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  tags = {
    Name      = local.fqdn
    ManagedBy = "terraform"
  }
}

# ─── S3 Bucket Policy (CloudFront OAC only) ─

resource "aws_s3_bucket_policy" "spa" {
  bucket = aws_s3_bucket.spa.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.spa.arn}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.spa.arn
        }
      }
    }]
  })
}

# ─── Cloudflare CNAME → CloudFront ───────

resource "cloudflare_record" "app" {
  zone_id = data.cloudflare_zone.main.id
  name    = var.subdomain
  content = aws_cloudfront_distribution.spa.domain_name
  type    = "CNAME"
  ttl     = 300
  proxied = false
}
