#!/bin/bash

# Vérifier si le fichier JAR existe
if [ ! -f "/forumSocialX.jar" ]; then
  echo "Erreur : /forumSocialX.jar n'existe pas."
  exit 1
fi

# Attendre que la base de données soit prête
/scripts/wait-for-it.sh dpg-ct1onv9opnds73fmddb0-a.oregon-postgres.render.com:5432 --timeout=60 --strict -- echo "Database is up!"

# Lancer l'application Spring Boot
java -Dserver.port=${PORT} -jar /forumSocialX.jar
