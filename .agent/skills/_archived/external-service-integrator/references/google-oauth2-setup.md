# Google API OAuth2 Setup

To use Google Drive or Gmail integration, you must configure a Google Cloud Project with OAuth2 credentials.

## Step-by-Step Configuration

1.  **Google Cloud Console**:
    *   Go to [Google Cloud Console](https://console.cloud.google.com/).
    *   Create a new Project.
    *   Enable APIs: Search for "Google Drive API" and "Gmail API" and enable them.

2.  **OAuth Consent Screen**:
    *   Configure the "OAuth Consent Screen".
    *   Set User Type to "External" (or "Internal" if you have a Google Workspace).
    *   Add Scopes: `.../auth/drive.file` and `.../auth/gmail.send/readonly`.

3.  **Credentials**:
    *   Click on "Create Credentials" -> "OAuth client ID".
    *   Application type: "Desktop app".
    *   Download the JSON file and rename it to `credentials.json`.
    *   Place it in `src/main/resources/`.

4.  **Local Token Storage**:
    *   The first time the application runs, it will open a browser for you to authorize the app.
    *   The resulting tokens are usually stored locally (e.g., in a `tokens/` directory) so subsequent runs don't require manual authorization.

## Scopes Reference
- **Drive**: `https://www.googleapis.com/auth/drive.file` (limited to files created by the app)
- **Gmail**: `https://www.googleapis.com/auth/gmail.readonly` or `https://www.googleapis.com/auth/gmail.send`.
