package com.web.forumSocialX.firebase;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Service
public class FirebaseService {

    private String bucketName = "forum-7ad15.appspot.com";  // Le nom de votre bucket Firebase

    // Méthode pour obtenir l'extension du fichier
    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // Méthode pour convertir un MultipartFile en fichier temporaire
    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }

    // Méthode pour télécharger un fichier sur Firebase Storage
    private String uploadFile(File file, String fileName, List<String> folderNames) throws IOException {
        // Créer le chemin complet du fichier dans le stockage
        String fullPath = String.join("/", folderNames) + "/" + fileName;
        BlobId blobId = BlobId.of(bucketName, fullPath);  // Utiliser votre nom de bucket Firebase
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();

        // Charger les credentials Firebase (clé de service)
        InputStream inputStream = FirebaseService.class.getClassLoader().getResourceAsStream("firebase-private-key.json");
        Credentials credentials = GoogleCredentials.fromStream(inputStream);

        // Connexion au service de stockage Firebase
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // Télécharger le fichier dans le bucket Firebase
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));

        // Retourner l'URL de téléchargement du fichier
        String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
        return String.format(DOWNLOAD_URL, bucketName, URLEncoder.encode(fullPath, StandardCharsets.UTF_8));
    }

    // Méthode pour uploader une image de profil (par exemple)
    public String uploadProfileImage(MultipartFile multipartFile, List<String> folderNames) {
        try {
            String fileName = UUID.randomUUID().toString().concat(this.getExtension(multipartFile.getOriginalFilename()));
            File file = this.convertToFile(multipartFile, fileName);
            String fileUrl = this.uploadFile(file, fileName, folderNames);
            file.delete();  // Supprimer le fichier temporaire après l'upload
            return fileUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Autres méthodes spécifiques de téléchargement de fichiers
    public String uploadFile(MultipartFile file, List<String> folderNames) {
        try {
            String fileName = file.getOriginalFilename();
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));
            File tempFile = this.convertToFile(file, fileName);
            String fileUrl = this.uploadFile(tempFile, fileName, folderNames);
            tempFile.delete();  // Supprimer le fichier temporaire
            return fileUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Vous pouvez ajouter des méthodes supplémentaires selon vos besoins
}
