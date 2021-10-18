FROM cifuzz/jazzer as jazzer

FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y curl openjdk-11-jdk-headless

WORKDIR /app
RUN curl -L 'https://github.com/coursier/coursier/releases/download/v2.0.16/coursier.jar' -o coursier.jar && \
    chmod +x coursier.jar

COPY entrypoint.sh /app/
COPY --from=jazzer /app/jazzer_agent_deploy.jar /app/jazzer_driver /app/

WORKDIR /fuzzing
ENTRYPOINT ["/app/entrypoint.sh"]
