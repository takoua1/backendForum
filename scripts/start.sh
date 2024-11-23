#!/bin/bash

# Vérifier si le fichier JAR existe
if [ ! -f "/forumSocialX.jar" ]; then
  echo "Erreur : /forumSocialX.jar n'existe pas."
  exit 1
fi

# Attendre que la base de données soit prête
/scripts/wait-for-it.sh db:5432 -t 30 -- echo "Database is up!"

# Démarrer l'application
java -jar /forumSocialX.jar
