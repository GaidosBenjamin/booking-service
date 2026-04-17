FROM public.ecr.aws/amazoncorretto/amazoncorretto:26-al2023-headless AS builder

WORKDIR /build

COPY pom.xml .
COPY booking-api/pom.xml booking-api/
COPY booking-data/pom.xml booking-data/
COPY booking-service/pom.xml booking-service/

RUN yum install -y maven && yum clean all

RUN mvn dependency:go-offline -pl booking-service -am -q

COPY booking-api/src booking-api/src
COPY booking-data/src booking-data/src
COPY booking-service/src booking-service/src

RUN mvn clean package -pl booking-service -am -DskipTests -q

FROM amazoncorretto:21-headless

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

WORKDIR /app

COPY --from=builder /build/booking-service/target/booking-service-*.jar app.jar

# ─── Memory Configuration ────────────────────────────────────────────────────
# UseContainerSupport: honours cgroup memory limits (on by default in JDK 11+)
# MaxRAMPercentage:    caps heap at 75 % of container memory limit
# MinRAMPercentage:    keeps heap at least 50 % of container memory limit
# MaxMetaspaceSize:    prevents Metaspace from growing unboundedly
# -XX:+ExitOnOutOfMemoryError: crash fast instead of limping on OOM
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:MinRAMPercentage=50.0 \
  -XX:MaxMetaspaceSize=192m \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
