# FCM production DB settings

FCM uses the same Parameter Store entries as the mail and main production
services. Do not create FCM-specific copies. In `ap-northeast-2`, the FCM
container reads these existing `SecureString` parameters on startup:

| Existing Parameter Store name | Value |
| --- | --- |
| `/bom-bom/prod/RDS_ENDPOINT` | RDS hostname, without a port |
| `/bom-bom/prod/DATABASE` | Database name |
| `/bom-bom/prod/MYSQL_USER` | DB username |
| `/bom-bom/prod/MYSQL_PASSWORD` | Current DB password |

The JDBC URL is composed with port `3306` and MySQL connection options. When
the password changes, update the existing `/bom-bom/prod/MYSQL_PASSWORD` value.
No separate FCM parameter is needed.

## GitHub Actions and YAML

The FCM build still reads the rest of its production configuration from the
`FCM_APPLICATION_PROD_YML` GitHub Secret. Before packaging, the workflow removes
`spring.datasource.url`, `spring.datasource.username`, and
`spring.datasource.password` from the generated configuration. Thus, old DB
credentials still in that GitHub Secret do not enter the JAR or image. You may
also remove those three fields from the GitHub Secret itself.

The FCM image starts with the `parameter-store` Spring profile. Local and test
profiles have Parameter Store disabled.

## IAM and redeploy

The FCM deployment runner needs AWS CLI and permission to read the four
parameters under `/bom-bom/prod/` with `ssm:GetParametersByPath`. The FCM
container also needs AWS credentials from its EC2 instance role to read those
parameters at startup. These can be different IAM principals: grant the
parameter read access to both the runner identity (for the pre-deploy check)
and the EC2 instance role (for application startup). If the values use a
customer-managed KMS key, the relevant identities also need `kms:Decrypt`.

The container must be able to reach SSM and the EC2 instance metadata endpoint.
If the EC2 role credentials are delivered through IMDSv2, check the metadata
response hop limit for the Docker network; a container may require a hop limit
of at least 2.

The workflow checks access to all four parameters before stopping the running
container. After updating `MYSQL_PASSWORD`, merge the change into `fcm-server`
to build a clean image and redeploy. The DB values are not packaged into the
image or deployment bundle.
