# FCM production database settings

The FCM deployment reads database connection settings from AWS Systems Manager
Parameter Store on the self-hosted `fcm` runner. The values are passed to the
container as environment variables; they are not needed while building the JAR
or Docker image.

## Parameter Store entries

Create these three parameters in `ap-northeast-2`, using type `SecureString`:

| Parameter name | Value |
| --- | --- |
| `/bom-bom/fcm/prod/SPRING_DATASOURCE_URL` | Full JDBC URL, including host, port, database name, and required connection options |
| `/bom-bom/fcm/prod/SPRING_DATASOURCE_USERNAME` | FCM database username |
| `/bom-bom/fcm/prod/SPRING_DATASOURCE_PASSWORD` | Current FCM database password |

The FCM self-hosted Actions runner needs the AWS CLI and an instance role that
allows `ssm:GetParameter` on these parameter ARNs. If the parameters use a
customer-managed KMS key, the role also needs `kms:Decrypt` for that key.

The parameter path and region are set in `.github/workflows/fcm-deploy.yml` as
`FCM_DB_PARAMETER_PATH` and `AWS_REGION`. Update those workflow values if the
parameters are stored elsewhere.

## Production YAML in GitHub Secrets

Edit the `FCM_APPLICATION_PROD_YML` GitHub Secret. Keep its other application
settings, but replace its datasource values with these references:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

The Compose file supplies those Spring environment variables from SSM at
container startup. The old image still contains the old YAML until the updated
workflow has built and deployed a new image, so rotate the database password
at the database and update these SSM parameters before redeploying.
