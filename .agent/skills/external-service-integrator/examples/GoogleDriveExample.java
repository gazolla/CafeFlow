package io.external.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Example of Google Drive service integration.
 */
public class GoogleDriveExample {

    public Drive getDriveService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("JAutomation")
                .build();
    }

    public void uploadFile(String filePath, String driveFolderName) throws IOException, GeneralSecurityException {
        Drive service = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(new java.io.File(filePath).getName());

        java.io.File filePathSource = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/octet-stream", filePathSource);

        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        System.out.println("File ID: " + file.getId());
    }
}
