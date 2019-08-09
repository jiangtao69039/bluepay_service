#/bin/bash
PROJECT_NAME=bluepay_service



REMOTE_FILE=${PROJECT_NAME}.jar
LOCAL_FILE=${PROJECT_NAME}-${NOW}.jar

NOW=$(date +"%Y%m%dT%H%M%S")

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
ARCHIVE=${SCRIPTPATH}/archives

REMOTE_FILE=${PROJECT_NAME}.jar
LOCAL_FILE=${ARCHIVE}/${PROJECT_NAME}-${NOW}.jar

echo $ARCHIVE
mkdir -p $ARCHIVE
aws s3 cp s3://panda-deploy/global/${PROJECT_NAME}/${REMOTE_FILE} ${LOCAL_FILE} --region=ap-southeast-1 \
  && ln -sf ${LOCAL_FILE} ${SCRIPTPATH}/${PROJECT_NAME}.jar


echo "sudo systemctl restart ${PROJECT_NAME}"
echo "sudo systemctl status ${PROJECT_NAME}"

