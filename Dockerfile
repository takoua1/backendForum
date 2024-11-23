# Étape 1 : Utiliser Maven pour compiler le projet
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers source
COPY pom.xml .
COPY src ./src

# Compiler et packager l'application
RUN mvn clean package -DskipTests

# Étape 2 : Image pour exécuter l'application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copier le JAR depuis l'étape précédente
COPY --from=build /app/target/forumSocialX-0.0.1-SNAPSHOT.jar /app/forumSocialX.jar

# Ajouter bash pour le script
RUN apk add --no-cache bash

# Copier le script wait-for-it.sh et le script de démarrage
COPY ./scripts/wait-for-it.sh /scripts/wait-for-it.sh
RUN chmod +x /scripts/wait-for-it.sh

COPY ./scripts/start.sh /start.sh
RUN chmod +x /start.sh

# Démarrer l'application avec le script
ENTRYPOINT ["/bin/sh", "/start.sh"]
