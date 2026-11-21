# Используем официальный образ OpenJDK 21 от Oracle (на базе Debian)
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR и модель как файлы
COPY build/libs/*.jar app.jar

# Удаляем неиспользуемый бинарник pebble, который поставляется в ubuntu:26.04 и содержит уязвимость.
RUN rm -f /usr/bin/pebble && rm -rf /var/lib/pebble

# Открываем порт, на котором работает приложение
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]