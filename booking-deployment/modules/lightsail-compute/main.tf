# ─── Container Deployment ────────────────

resource "aws_lightsail_container_service_deployment_version" "app" {
  service_name = var.container_service_name

  container {
    container_name = var.project_name
    image          = var.container_image

    ports = {
      (var.container_port) = "HTTP"
    }

    environment = var.environment
  }

  public_endpoint {
    container_name = var.project_name
    container_port = var.container_port

    health_check {
      path                = "/actuator/health"
      success_codes       = "200"
      interval_seconds    = 10
      timeout_seconds     = 8
      healthy_threshold   = 2
      unhealthy_threshold = 10
    }
  }
}
