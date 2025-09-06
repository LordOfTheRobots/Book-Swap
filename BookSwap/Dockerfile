# Используем многоэтапную сборку
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (это кэшируется)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Этап выполнения
FROM openjdk:17-jdk-slim

WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Копируем jar файл из builder этапа
COPY --from=builder /app/target/*.jar app.jar

# Создаем директории для логов с правильными правами
RUN mkdir -p logs && chown -R spring:spring /app

USER spring:spring

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"] 