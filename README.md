# Crydera Core

Core бэкенд платежного шлюза

Состоит из двух Gradle-модулей:  
`core-merchant` (личный кабинет, кошельки, API ключи)  
`core-payment` (создание и трекинг платежей)

## Запуск Gradle

```bash
./gradlew bootRun
```

## Запуск Docker

```bash
docker build -f backend/Dockerfile -t crydera-core .
```

Пример запуска:

```bash
docker run --rm -p 8088:8088 \
  -e JWT_SECRET=$(openssl rand -hex 32) \
  -e CORE_DB_URL=jdbc:postgresql://host.docker.internal:26257/crydera_core?sslmode=disable \
  -e CORE_DB_USER=crydera \
  -e REDIS_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP=host.docker.internal:9092 \
  -e TRON_SIDECAR_GRPC=static://host.docker.internal:50051 \
  crydera-core
```

API `http://localhost:8088`  
Swagger `/swagger-ui/index.html`
