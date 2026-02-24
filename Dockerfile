
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY backend/gradlew backend/gradlew
COPY backend/gradle backend/gradle
COPY backend/build.gradle backend/settings.gradle backend/

COPY infra/proto infra/proto

COPY backend/core-merchant/build.gradle backend/core-merchant/
COPY backend/core-payment/build.gradle backend/core-payment/

RUN chmod +x backend/gradlew \
 && cd backend \
 && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY backend/src backend/src
COPY backend/core-merchant/src backend/core-merchant/src
COPY backend/core-payment/src backend/core-payment/src

RUN cd backend && ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /workspace/backend/build/libs/crydera-core.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java","-jar","/app/app.jar"]
