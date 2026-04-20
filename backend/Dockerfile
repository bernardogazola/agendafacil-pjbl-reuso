# syntax=docker/dockerfile:1.7
# Imagem multi-stage para o backend Quarkus. Stage builder compila o fast-jar,
# stage runtime serve em JRE enxuto com curl disponível para o healthcheck.

# Stage 1: builder
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Cache deps
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp -q dependency:go-offline || true

# Copia o código e package o fast-jar (sem testes, CI rodaria separado).
COPY src/ src/
RUN ./mvnw -B -ntp package -DskipTests -Dquarkus.package.jar.type=fast-jar

# Stage 2: runtime
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
 && addgroup -S agenda \
 && adduser -S -G agenda agenda

WORKDIR /deployments

# Estrutura do fast-jar em 4 layers para melhor cache de imagem.
COPY --from=builder --chown=agenda:agenda /build/target/quarkus-app/lib/      ./lib/
COPY --from=builder --chown=agenda:agenda /build/target/quarkus-app/*.jar     ./
COPY --from=builder --chown=agenda:agenda /build/target/quarkus-app/app/      ./app/
COPY --from=builder --chown=agenda:agenda /build/target/quarkus-app/quarkus/  ./quarkus/

USER agenda

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV QUARKUS_HTTP_HOST=0.0.0.0

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar quarkus-run.jar"]