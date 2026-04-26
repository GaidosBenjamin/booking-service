output "github_actions_role_arn" {
  description = "Set this as the AWS_ROLE_ARN secret in your GitHub repository"
  value       = module.iam-github-actions.role_arn
}
