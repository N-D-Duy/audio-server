# Sử dụng hình ảnh Gradle để build dự án
FROM gradle:8.4-jdk17 AS builder

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file build.gradle, settings.gradle và source code
COPY build.gradle settings.gradle ./
COPY src ./src


# Build dự án với ShadowJar
RUN gradle clean shadowJar -x test

# Sử dụng hình ảnh JRE nhỏ gọn để chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file JAR từ giai đoạn builder
COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir /app/logs
RUN chmod 777 /app/logs

# Sao chép file run.sh vào container
COPY run.sh ./run.sh
RUN chmod +x ./run.sh

COPY config.properties ./config.properties


# Thêm người dùng không phải root để bảo mật (tùy chọn)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Thiết lập script làm lệnh chạy
ENTRYPOINT ["./run.sh"]

