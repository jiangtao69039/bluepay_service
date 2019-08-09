#!/bin/bash

export AWS_ACCESS_KEY_ID=${GLOBAL_DEPOLY_AWS_ACCESS_KEY_ID}
export AWS_SECRET_ACCESS_KEY=${GLOBAL_DEPOLY_AWS_SECRET_ACCESS_KEY}
export AWS_REGION=${GLOBAL_DEPOLY_AWS_REGION}

PROJECT_NAME=bluepay_service
S3_PREFIX="s3://panda-deploy/global"


./gradlew build -x test -x runFormat && \
  aws s3 cp build/libs/${PROJECT_NAME}.jar ${S3_PREFIX}/${PROJECT_NAME}/${PROJECT_NAME}.jar --region=${AWS_REGION}

# 在 jenkins 中构建时
# 1. Build -> add "inject environment variables" => Properties File Path set: /opt/data/jenkins-etc/global_aws_env.properties
# 2. Build -> Execute Shell Command: /bin/sh jenkins-build.sh