output "certificate_name" {
  description = "Name of the Lightsail certificate (only emitted once the cert is ISSUED)"
  value       = aws_lightsail_certificate.this.name
  depends_on  = [null_resource.wait_for_validation]
}

output "fqdn" {
  description = "Fully qualified domain name the certificate covers"
  value       = aws_lightsail_certificate.this.domain_name
}
