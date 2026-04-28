
# ─── Module: ACM Wildcard Certificate ────

module "acm-certificate" {
  source = "../../modules/acm-certificate"

  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }

  domain = "bbso.dev"
}

# # ─── Module: Frontend SPA (camp-dev.bbso.dev) ─
#
# module "cloudfront-spa-dev" {
#   source = "../../modules/cloudfront-spa"
#
#   project_name    = var.project_name
#   domain          = "bbso.dev"
#   subdomain       = "camp-dev"
#   certificate_arn = module.acm-certificate.certificate_arn
# }

# ─── Module: Frontend SPA (camp.bbso.dev) ─

module "cloudfront-spa" {
  source = "../../modules/cloudfront-spa"

  project_name    = var.project_name
  domain          = "bbso.dev"
  subdomain       = "camp"
  certificate_arn = module.acm-certificate.certificate_arn
}