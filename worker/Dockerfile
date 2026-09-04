# Используем официальный образ OpenJDK 21 от Oracle (на базе Debian)
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR и модель как файлы
COPY build/libs/*.jar app.jar

# Открываем порт, на котором работает приложение
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]