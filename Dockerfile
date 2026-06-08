# --- Stage 1: Build ứng dụng ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Sao chép file cấu hình pom.xml và tải trước các thư viện để tận dụng cache
COPY pom.xml .
RUN mvn dependency:go-offline

# Sao chép toàn bộ mã nguồn và biên dịch ra file .jar
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Khởi chạy ứng dụng ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Sao chép file .jar đã build từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar

# Mở cổng mạng
EXPOSE 8080

# Lệnh khởi chạy ứng dụng Java
ENTRYPOINT ["java", "-jar", "app.jar"]