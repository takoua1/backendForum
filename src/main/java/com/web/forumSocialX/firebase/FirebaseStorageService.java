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
public class FirebaseStorageService {
    private String bucketName = "forum-7ad15.appspot.com"; // Injected from application.properties or application.yml

  /*  public String uploadImage(String folderName, String fileName, MultipartFile file) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Build the full path of the file in Firebase Storage
        String fullPath = folderName + "/" + fileName;

        // Define the BlobId, including the bucket name and the full path
        BlobId blobId = BlobId.of(bucketName, fullPath);

        // Create BlobInfo to set the content type
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(Objects.requireNonNull(file.getContentType()))
                .build();

        // Upload the file to Firebase Storage
        storage.create(blobInfo, file.getBytes());

        // Generate and return the download URL
        return String.format("https://storage.googleapis.com/forum-7ad15.appspot.com/forum-7ad15-firebase-adminsdk-37o2u-4172d35119.json", bucketName, fullPath);
    }

    public byte[] downloadImage(String folderName, String fileName) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Build the full path of the file in Firebase Storage
        String fullPath = folderName + "/" + fileName;

        // Define the BlobId
        BlobId blobId = BlobId.of(bucketName, fullPath);

        // Download the file as a byte array
        return storage.readAllBytes(blobId);
    }

    public String uploadProfileImage(String userId, MultipartFile file) throws IOException {
        // Define a folder name for profile images (customize as needed)
        String folderName = "profile-images";

        // Generate a unique file name, you can use the user ID or any other identifier
        String fileName = userId + "_profile.jpg";

        // Call the generic uploadImage method
        return uploadImage(folderName, fileName, file);
    }*/
  private String uploadFile(File file, String fileName, List<String> folderNames) throws IOException {
      String fullPath = String.join("/", folderNames) + "/" + fileName;
      BlobId blobId = BlobId.of(bucketName, fullPath); // Replace with your bucker name
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
      InputStream inputStream = FirebaseStorageService.class.getClassLoader().getResourceAsStream("firebase-private-key.json"); // change the file name with your one
      Credentials credentials = GoogleCredentials.fromStream(inputStream);
      Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
      storage.create(blobInfo, Files.readAllBytes(file.toPath()));

      String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
      // Utilisez String.format pour insérer les valeurs de bucketName et fullPath dans l'URL
      return String.format(DOWNLOAD_URL, bucketName, URLEncoder.encode(fullPath, StandardCharsets.UTF_8));
  }
    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }


    public String upload(MultipartFile multipartFile,List<String> folderNames) {
        try {
            String fileName = multipartFile.getOriginalFilename();                        // to get original file name
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));  // to generated random string values for file name.

            File file = this.convertToFile(multipartFile, fileName);                      // to convert multipartFile to File
            String URL = this.uploadFile(file, fileName, folderNames);                                   // to get uploaded file link
            file.delete();
            return URL;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}


