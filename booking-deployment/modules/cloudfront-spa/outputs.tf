output "fqdn" {
  description = "Fully qualified domain name of the SPA"
  value       = local.fqdn
}

output "bucket_name" {
  description = "Name of the S3 bucket hosting the SPA assets"
  value       = aws_s3_bucket.spa.id
}

output "bucket_arn" {
  description = "ARN of the S3 bucket hosting the SPA assets"
  value       = aws_s3_bucket.spa.arn
}

output "distribution_id" {
  description = "CloudFront distribution ID (use for cache invalidations)"
  value       = aws_cloudfront_distribution.spa.id
}

output "distribution_domain_name" {
  description = "CloudFront distribution domain name (e.g., d123.cloudfront.net)"
  value       = aws_cloudfront_distribution.spa.domain_name
}
