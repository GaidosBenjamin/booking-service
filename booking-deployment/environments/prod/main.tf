# ─── Module: S3 Terraform State ──────────

module "s3-terraform-state" {
  source = "../../modules/s3-terraform-state"
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

# ─── Module: Container Service & Registry ─

module "lightsail-container" {
  source = "../../modules/lightsail-container"

  service_name = "${var.project_name}-service"
  power        = "micro" #1GB Ram, review GraalVM option with 500mb memory
  scale        = 1
  aws_region   = var.aws_region
}

# ─── Module: Compute (Deployment) ────────

# module "lightsail-compute" {
#   source = "../../modules/lightsail-compute"
#
#   service_name    = module.lightsail-container.service_name
#   project_name    = var.project_name
#   container_image = ":${module.lightsail-container.service_name}/booking-service:${var.container_image_tag}"
#   container_port  = 8080
#
#   environment = {
#     SPRING_DATASOURCE_URL      = "jdbc:postgresql://${module.lightsail-database.db_endpoint}:${module.lightsail-database.db_port}/${module.lightsail-database.db_name}?currentSchema=${module.lightsail-database.db_schema}"
#     SPRING_DATASOURCE_USERNAME = module.lightsail-database.db_username
#     SPRING_DATASOURCE_PASSWORD = module.lightsail-database.db_password
#     # BUCKET_NAME                = module.lightsail-storage.bucket_name
#     # BUCKET_ACCESS_KEY          = module.lightsail-storage.bucket_access_key_id
#     # BUCKET_SECRET_KEY          = module.lightsail-storage.bucket_secret_access_key
#   }
# }
#
# # ─── Module: Storage ─────────────────────
#
# module "lightsail-storage" {
#   source = "../../modules/lightsail-storage"
#
#   project_name           = var.project_name
#   container_service_name = module.lightsail-compute.container_service_name
# }
#
# # ─── Module: DNS ─────────────────────────
#
# module "cloudflare-dns" {
#   source = "../../modules/cloudflare-dns"
#
#   domain                = "bbso.dev"
#   subdomain             = "api"
#   container_service_url = module.lightsail-compute.container_service_url
# }
