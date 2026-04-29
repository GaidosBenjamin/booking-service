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
      # Check more frequently (every 15 seconds)
      interval_seconds    = 25
      # Give the ping 10s to respond
      timeout_seconds     = 10
      # It will now only take ~30 seconds to be marked healthy once Tomcat starts
      healthy_threshold   = 2
      # 15 seconds * 16 fails = 240 seconds (4 minutes) of allowed startup time
      unhealthy_threshold = 10
    }
  }
}
