
# ─── Module: Frontend SPA (camp.bbso.dev) ─

module "cloudfront-spa" {
  source = "../../modules/cloudfront-spa"

  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }

  project_name = var.project_name
  domain       = "bbso.dev"
  subdomain    = "camp"
}
