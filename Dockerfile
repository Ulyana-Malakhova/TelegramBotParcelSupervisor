# Используем образ OpenJDK
FROM openjdk:17
# Копируем файл pom.xml и собираем проект
COPY pom.xml .
# Добавляем зависимости
RUN mvn dependency:go-offline
# Копируем все остальные файлы проекта
COPY src ./src
# Собираем проект
RUN mvn package -DskipTests
# Указываем команду для запуска приложения
CMD ["java", "-jar", "out/artifacts/telegramBotParcelSupervisor_jar/telegramBotParcelSupervisor.jar"]