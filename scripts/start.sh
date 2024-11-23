#!/bin/bash

# Attendre que la base de données soit prête sur db:5432 avec un timeout de 30 secondes
/scripts/wait-for-it.sh db:5432 -t 30 -- echo "Database is up!"

# Démarrer l'application Spring Boot (en supposant que le fichier JAR se trouve dans le dossier target)
java -jar /forumSocialX.jar
ls -al /forumSocialX.jar
# Attendre un moment pour voir si l'application se lance
sleep 10

# Vérifier si l'application fonctionne
ps aux | grep 'java'  # Afficher les processus java en cours d'exécution