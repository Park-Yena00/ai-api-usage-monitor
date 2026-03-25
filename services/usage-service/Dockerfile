# CI 또는 로컬에서 이 디렉터리에서 ./gradlew bootJar 로 생성한 app.jar 를 포함한 컨텍스트로 빌드합니다.
# (settings.gradle 이 ../../libs/usage-events 를 포함함)
#
# 로컬 개발은 기본적으로 호스트에서 bootRun 하며, 루트 docker-compose.yml 에는 포함하지 않는다.
# 컨테이너로 실행 시 Postgres·RabbitMQ 호스트는 환경 변수로 맞출 것(예: host.docker.internal).

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY build/libs/app.jar app.jar

EXPOSE 8092
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
