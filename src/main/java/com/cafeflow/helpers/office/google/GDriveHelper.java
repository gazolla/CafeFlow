package com.cafeflow.helpers.office.google;

import com.cafeflow.core.base.BaseHelper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class GDriveHelper extends BaseHelper {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    @Value("${google.drive.application-name:CafeFlow}")
    private String applicationName;

    @Value("${google.drive.credentials-path:/credentials.json}")
    private String credentialsFilePath;

    @Value("${google.drive.tokens-directory:tokens}")
    private String tokensDirectoryPath;

    private Drive driveService;

    @Override
    protected String getServiceName() {
        return "google_drive";
    }

    @PostConstruct
    public void init() {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            this.driveService = new Drive.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                    .setApplicationName(applicationName)
                    .build();
            log.info("GDriveHelper initialized successfully.");
        } catch (Exception e) {
            log.warn("Failed to initialize GDriveHelper: {}. Ensure {} exists.", e.getMessage(), credentialsFilePath);
        }
    }

    private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
        InputStream in = GDriveHelper.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Lists files from Google Drive.
     */
    public List<File> listFiles(int pageSize) {
        if (driveService == null)
            return Collections.emptyList();
        return executeWithProtection("listFiles", () -> {
            FileList result = driveService.files().list()
                    .setPageSize(pageSize)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            return result.getFiles();
        });
    }

    /**
     * Uploads a file to Google Drive.
     */
    public File uploadFile(String name, String mimeType,
            com.google.api.client.http.AbstractInputStreamContent content) {
        if (driveService == null)
            return null;
        return executeWithProtection("uploadFile", () -> {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            return driveService.files().create(fileMetadata, content)
                    .setFields("id")
                    .execute();
        });
    }

    /**
     * Downloads file content.
     */
    public InputStream downloadFile(String fileId) {
        if (driveService == null)
            return null;
        return executeWithProtection("downloadFile",
                () -> driveService.files().get(fileId).executeMediaAsInputStream());
    }
}
