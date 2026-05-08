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

RUN groupadd -f docker && usermod -aG docker jenkins

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER root
ENTRYPOINT ["/entrypoint.sh"]