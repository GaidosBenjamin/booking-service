output "bucket_name" {
  description = "Lightsail bucket name"
  value       = aws_lightsail_bucket.images.name
}

output "bucket_url" {
  description = "Lightsail bucket base URL — append /<key> to get the public URL of any object"
  value       = aws_lightsail_bucket.images.url
}

output "bucket_access_key_id" {
  description = "Access key ID for the bucket"
  value       = aws_lightsail_bucket_access_key.app.access_key_id
}

output "bucket_secret_access_key" {
  description = "Secret access key for the bucket"
  value       = aws_lightsail_bucket_access_key.app.secret_access_key
  sensitive   = true
}
