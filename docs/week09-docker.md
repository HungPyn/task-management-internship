# Docker Hóa Ứng Dụng

## Mục tiêu
- Docker hóa ứng dụng Spring Boot.
- Dùng Docker Compose để chạy ứng dụng cùng với MySQL.
- Deploy ứng dụng trên Heroku hoặc một VPS tùy theo năng lực.

---

## 1. Dockerfile cho Spring Boot App

- Tạo file `Dockerfile` trong thư mục gốc của project Spring Boot:

📁 [Dockerfile](../Dockerfile)

```Dockerfile
# Build stage: dùng image có Maven và JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage: chạy với JDK 17 hoặc 19
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
---
## 2. .env cài đặt biến môi trường
Tạo file `.env`
📁 [env](../.env)
```env
MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=task_management

SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/task_management
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456

JWT_SECRET=VfSv+WcFQhGnYTf6hVqq9BsJkQdmXe4B0E8VNeI8RgTclWaWy6vCugd7zOUkK6lYOI6O1DGfi1IedFAIMXA3cQ==
JWT_EXPIRATION=86400000

```
---
## 3. Docker Compose để chạy ứng dụng và MySQL

Tạo file `docker-compose.yml`:
📁 [docker-compose](../docker-compose.yml)
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    ports:
      - "3307:3306"
    networks:
      - backend

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: springboot-app
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    networks:
      - backend
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}

networks:
  backend:

```
---

## 4. Deploy ứng dụng

### Cách 2: Deploy lên VPS
- SSH vào VPS và cài Docker + Docker Compose.
- Clone repo và chạy:
  ```bash
  docker-compose up -d --build
  ```
---
## 5. Hình ảnh chạy thành công trên docker desktop
![Chạy thành công bằng Docker](./images/docker.png)
---
## Kết Quả Đạt Được 🎯
- [x] Ứng dụng Spring Boot đã được Docker hóa thành công bằng cách sử dụng Dockerfile hai tầng (multi-stage build).

- [x] Ứng dụng và cơ sở dữ liệu MySQL chạy cùng nhau bằng Docker Compose, cho phép dễ dàng quản lý và triển khai môi trường phát triển và sản xuất.

- [x] Biến môi trường được cấu hình qua .env hoặc cấu hình nội bộ giúp tăng tính bảo mật và linh hoạt.

- [x] Triển khai thành công trên Heroku hoặc một VPS với Docker, chứng minh ứng dụng có thể chạy độc lập trên môi trường cloud hoặc server cá nhân.

- [x] Hệ thống sẵn sàng cho CI/CD và các môi trường staging/production nhờ khả năng container hóa.