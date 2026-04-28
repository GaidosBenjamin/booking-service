output "certificate_arn" {
  description = "ARN of the validated wildcard ACM certificate (*.{domain})"
  value       = aws_acm_certificate_validation.wildcard.certificate_arn
}
