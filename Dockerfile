# Étape 1 : Spécifier une image de base JDK
FROM eclipse-temurin:17-jdk-alpine

# Étape 2 : Installer bash (car Alpine utilise sh par défaut)
RUN apk add --no-cache bash

# Étape 3 : Copier le fichier JAR dans le conteneur
COPY target/forumSocialX-0.0.1-SNAPSHOT.jar /forumSocialX.jar

# Vérification que le fichier JAR est bien copié
RUN ls -al /forumSocialX.jar

# Étape 4 : Copier le script wait-for-it.sh dans le conteneur
COPY ./scripts/wait-for-it.sh /scripts/wait-for-it.sh
RUN chmod +x /scripts/wait-for-it.sh

# Étape 5 : Copier le script start.sh dans le conteneur
COPY scripts/start.sh /start.sh

# Étape 6 : Donner les bonnes permissions d'exécution pour start.sh
RUN chmod +x /start.sh

# Étape 7 : Utiliser le script start.sh comme point d'entrée
ENTRYPOINT ["/bin/sh", "/start.sh"]
