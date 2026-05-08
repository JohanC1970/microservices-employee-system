FROM jenkins/jenkins:lts-jdk21

RUN jenkins-plugin-cli --plugins \
    workflow-aggregator \
    docker-commons \
    docker-workflow \
    pipeline-stage-view \
    git \
    configuration-as-code \
    job-dsl \
    sonar \
    jacoco \
    junit \
    build-timeout \
    timestamper

USER root

RUN apt-get update && \
    apt-get install -y docker.io curl maven && \
    rm -rf /var/lib/apt/lists/*

# Instalar docker-compose
RUN curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-linux-x86_64" \
    -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

RUN groupadd -f docker && usermod -aG docker jenkins

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER root
ENTRYPOINT ["/entrypoint.sh"]