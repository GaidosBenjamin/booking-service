# Used to reference the service in a deployment resource
output "service_name" {
  description = "Name of the Lightsail Container Service"
  value       = aws_lightsail_container_service.this.name
}

# Used to point DNS / other modules at this service
output "service_url" {
  description = "Public HTTPS URL of the Lightsail Container Service"
  value       = aws_lightsail_container_service.this.url
}

output "service_arn" {
  description = "ARN of the Lightsail Container Service"
  value       = aws_lightsail_container_service.this.arn
}
