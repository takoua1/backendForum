package com.web.forumSocialX.firebase;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class FirebaseService {

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final Bucket bucket = storage.get("firebase-private-key.json");

    public String uploadProfileImage(Path filePath) throws IOException {
        String blobName =  filePath.getFileName().toString();
        Blob blob = bucket.create(blobName, Files.readAllBytes(filePath));
        return blob.getSelfLink();
    }



}


