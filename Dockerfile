# Étape 1 : Build avec Maven
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le POM et les sources
COPY pom.xml .
COPY src ./src

# Compiler le projet
RUN mvn clean package -DskipTests

# Étape 2 : Exécution avec une image légère
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Mettre à jour les miroirs et installer bash
RUN sed -i 's|http://dl-cdn.alpinelinux.org/alpine/|https://mirror.yandex.ru/mirrors/alpine/|g' /etc/apk/repositories && \
    apk add --no-cache bash

# Copier le JAR depuis l'étape précédente
COPY --from=build /app/target/forumSocialX-0.0.1-SNAPSHOT.jar /forumSocialX.jar

# Copier les scripts nécessaires
COPY ./scripts/wait-for-it.sh /scripts/wait-for-it.sh
COPY ./scripts/start.sh /start.sh

# Donner les permissions d'exécution
RUN chmod +x /scripts/wait-for-it.sh /start.sh
EXPOSE 8084
# Configurer l'entrypoint
ENTRYPOINT ["/bin/bash", "/start.sh"]
