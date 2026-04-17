# ─── Lightsail Object Storage Bucket ─────

resource "aws_lightsail_bucket" "images" {
  name      = "${var.project_name}-images"
  bundle_id = var.bucket_bundle_id

  tags = {
    Name      = "${var.project_name}-images"
    ManagedBy = "terraform"
  }
}

# ─── Public Read Access for Images ───────

resource "aws_lightsail_bucket_access_key" "app" {
  bucket_name = aws_lightsail_bucket.images.name
}

resource "aws_lightsail_bucket_resource_access" "container" {
  bucket_name   = aws_lightsail_bucket.images.name
  resource_name = var.container_service_name
}
