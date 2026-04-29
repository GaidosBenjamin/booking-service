# ─── Module: S3 Terraform State ──────────

module "s3-terraform-state" {
  source = "../../modules/s3-terraform-state"
}

# ─── Module: GitHub Actions IAM ──────────

module "iam-github-actions" {
  source = "../../modules/iam-github-actions"

  github_org   = var.github_org
  github_repos = ["booking-service", "booking-web"]
}

# ─── Module: Database ────────────────────

module "lightsail-database" {
  source = "../../modules/lightsail-database"

  db_bundle_id           = "micro_2_0"
  project_name           = var.project_name
  db_name                = "bbso"
  db_schema              = "booking-service"
  db_blueprint_id        = "postgres_18"
  db_publicly_accessible = true
}

# ─── Module: SSL Certificate ─────────────

module "lightsail-certificate" {
  source = "../../modules/lightsail-certificate"

  certificate_name = "${var.project_name}-cert"
  domain           = "bbso.dev"
  subdomain        = "api"
  aws_region       = var.aws_region
}

# ─── Module: Container Service & Registry ─

module "lightsail-container" {
  source = "../../modules/lightsail-container"

  service_name     = "${var.project_name}-container"
  power            = "micro" #1GB Ram, review GraalVM option with 500mb memory
  scale            = 1
  aws_region       = var.aws_region
  certificate_name = module.lightsail-certificate.certificate_name
  custom_domain    = module.lightsail-certificate.fqdn
}

# ─── Module: Compute (Deployment) ────────

module "lightsail-compute" {
  source = "../../modules/lightsail-compute"

  container_service_name = module.lightsail-container.service_name
  project_name           = var.project_name
  container_image        = ":${module.lightsail-container.service_name}.booking-service.${var.container_image_tag}"
  container_port         = 8080

  environment = {
    DB_URL      = "jdbc:postgresql://${module.lightsail-database.db_endpoint}:${module.lightsail-database.db_port}/${module.lightsail-database.db_name}?currentSchema=${module.lightsail-database.db_schema}"
    DB_USER     = module.lightsail-database.db_username
    DB_PASSWORD = module.lightsail-database.db_password

    SMTP_USER     = var.smtp_user
    SMTP_PASSWORD = var.smtp_password

    JWT_SECRET = var.jwt_secret

    CORS_ALLOWED_ORIGINS = var.cors_allowed_origins

    STRIPE_API_KEY        = var.stripe_api_key
    STRIPE_WEBHOOK_SECRET = var.stripe_webhook_secret
    STRIPE_SUCCESS_URL    = var.stripe_success_url
    STRIPE_CANCEL_URL     = var.stripe_cancel_url

    STRIPE_DONATION_SUCCESS_URL = var.stripe_donation_success_url
    STRIPE_DONATION_CANCEL_URL  = var.stripe_donation_cancel_url

    MAIL_FROM  = var.mail_from
    MAIL_BRAND = var.mail_brand

    BGAIDOS_LOG_LEVEL = var.log_level
  }
}

# ─── Module: Storage ─────────────────────

module "lightsail-storage" {
  source = "../../modules/lightsail-storage"

  project_name = var.project_name
}

# ─── Module: DNS ─────────────────────────

module "cloudflare-dns" {
  source = "../../modules/cloudflare-dns"

  domain                = "bbso.dev"
  subdomain             = "api"
  container_service_url = module.lightsail-container.service_url
  proxied               = false
}