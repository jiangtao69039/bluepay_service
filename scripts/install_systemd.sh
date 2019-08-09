#!/bin/bash

LISTEN_PORT=8094
PROJECT_NAME="bluepay_service"
SERVICE_FILE="/etc/systemd/system/${PROJECT_NAME}.service"
JAVA_EXEC=$(readlink -f $(which java))
WORK_DIR="/srv/${PROJECT_NAME}"

cat << EOF > $SERVICE_FILE
[Service]
Environment=TZ='Asia/Shanghai'
Environment=JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ExecStart=${JAVA_EXEC} -jar ${WORK_DIR}/${PROJECT_NAME}.jar --server.port=${LISTEN_PORT} --spring.profiles.active=prod

Restart=always
StandardOutput=syslog
StandardError=syslog

User=www
Group=www

[Install]
WantedBy=multi-user.target

# sudo systemctl enable  ${PROJECT_NAME}.service
# sudo systemctl daemon-reload
# sudo systemctl  restart ${PROJECT_NAME}
EOF

sudo systemctl enable ${PROJECT_NAME}.service
sudo systemctl daemon-reload
sudo systemctl start  ${PROJECT_NAME} -l


