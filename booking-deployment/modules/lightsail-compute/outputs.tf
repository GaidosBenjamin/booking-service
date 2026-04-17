output "container_service_url" {
  description = "Public URL of the Lightsail Container Service"
  value       = aws_lightsail_container_service.app.url
}

output "container_service_name" {
  description = "Name of the Lightsail Container Service"
  value       = aws_lightsail_container_service.app.name
}
