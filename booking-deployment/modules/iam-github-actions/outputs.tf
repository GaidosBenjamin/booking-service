output "role_arn" {
  description = "ARN of the IAM role — set this as the AWS_ROLE_ARN secret in GitHub"
  value       = aws_iam_role.github_actions.arn
}
