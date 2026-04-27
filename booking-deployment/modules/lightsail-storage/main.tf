data "aws_caller_identity" "current" {}

# ─── Lightsail Object Storage Bucket ─────

resource "aws_lightsail_bucket" "images" {
  # Lightsail bucket names are globally unique across all AWS accounts;
  # appending the account ID avoids collisions with common names.
  name      = "${var.project_name}-images-${data.aws_caller_identity.current.account_id}"
  bundle_id = var.bucket_bundle_id

  tags = {
    Name      = "${var.project_name}-images"
    ManagedBy = "terraform"
  }
}

# ─── App Access ──────────────────────────

resource "aws_lightsail_bucket_access_key" "app" {
  bucket_name = aws_lightsail_bucket.images.name
}

# ─── Public Read Access ───────────────────
# Terraform's aws_lightsail_bucket has no access_rules attribute; set via CLI.

resource "null_resource" "public_read" {
  depends_on = [aws_lightsail_bucket.images]

  triggers = {
    bucket_name = aws_lightsail_bucket.images.name
  }

  provisioner "local-exec" {
    command = <<-EOT
      aws lightsail update-bucket \
        --bucket-name ${aws_lightsail_bucket.images.name} \
        --access-rules '{"getObject":"public","allowPublicOverrides":false}'
    EOT
  }
}
