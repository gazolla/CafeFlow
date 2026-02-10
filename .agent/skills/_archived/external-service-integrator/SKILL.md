---
name: external-service-integrator
description: Patterns for integrating Google Drive, Gmail, Search, and RSS in Java.
---

# External Service Integrator Skill

This skill provides patterns for integrating common external services using standard Java libraries and Google SDKs.

## 1. Google Drive Integration

### Setup & Credentials
Add dependencies and place `credentials.json` in `src/main/resources`.

```java
@Configuration
public class GoogleDriveConfig {
    @Bean
    public Drive driveService() throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            getClass().getResourceAsStream("/credentials.json"))
            .createScoped(DriveScopes.DRIVE_FILE);
        
        return new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
            .setApplicationName("JAutomation")
            .build();
    }
}
```

### Pattern: Search and Download
```java
public void findAndDownload(Drive driveService, String query) throws IOException {
    FileList result = driveService.files().list()
        .setQ("name contains '" + query + "'")
        .setSpaces("drive")
        .setFields("files(id, name)")
        .execute();

    for (File file : result.getFiles()) {
        OutputStream outputStream = new FileOutputStream(new java.io.File(file.getName()));
        driveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
    }
}
```

## 2. Gmail Integration

### Pattern: Searching Emails with Filters
```java
public List<Message> listMessagesMatchingQuery(Gmail service, String query) throws IOException {
    ListMessagesResponse response = service.users().messages().list("me").setQ(query).execute();
    List<Message> messages = new ArrayList<>();
    while (response.getMessages() != null) {
        messages.addAll(response.getMessages());
        if (response.getNextPageToken() != null) {
            String pageToken = response.getNextPageToken();
            response = service.users().messages().list("me").setQ(query).setPageToken(pageToken).execute();
        } else {
            break;
        }
    }
    return messages;
}
```

## 3. HttpClient for Search (Generic API)

### Pattern: Streaming Large Responses
```java
public void downloadLargeFile(String url, Path target) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

    HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target));
}
```

## 4. JSON API Integration (Preferred)

> [!IMPORTANT]
> **Prefer JSON APIs over RSS/XML feeds** when available. Most services (Reddit, GitHub, etc.) provide JSON endpoints that are more reliable and easier to parse.

### Pattern: Fetching JSON with User-Agent (Reddit Example)

> [!CAUTION]
> **Always set a User-Agent header.** Many services (like Reddit) block requests without proper User-Agent headers with 403/429 errors.

```java
public List<Post> fetchPosts(ObjectMapper objectMapper) throws IOException {
    String url = "https://www.reddit.com/r/example/top/.json";
    URLConnection connection = URI.create(url).toURL().openConnection();
    
    // CRITICAL: Set User-Agent to avoid 403 Forbidden
    connection.setRequestProperty("User-Agent", "MyApp/1.0 (Java; com.myapp)");

    try (InputStream is = connection.getInputStream()) {
        JsonNode root = objectMapper.readTree(is);
        JsonNode children = root.path("data").path("children");

        List<Post> posts = new ArrayList<>();
        for (JsonNode child : children) {
            if (posts.size() >= 5) break;
            JsonNode data = child.path("data");
            posts.add(new Post(
                data.path("title").asText(),
                data.path("url").asText(),
                data.path("ups").asInt()  // upvotes
            ));
        }
        return posts;
    }
}
```

## 5. RSS Feed Integration (Legacy)

> [!WARNING]
> RSS feeds often lack data (e.g., upvotes, likes) and can return inconsistent formats. Use JSON APIs when available.

### Pattern: Feed Caching and Filtering
```java
public List<SyndEntry> getLatestEntries(String url, LocalDateTime since) throws Exception {
    // Use InputStream instead of deprecated XmlReader(URL)
    URLConnection connection = URI.create(url).toURL().openConnection();
    connection.setRequestProperty("User-Agent", "MyApp/1.0");
    
    try (InputStream is = connection.getInputStream()) {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(is));
        return feed.getEntries().stream()
            .filter(entry -> {
                Date pubDate = entry.getPublishedDate();
                LocalDateTime entryDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return entryDate.isAfter(since);
            })
            .collect(Collectors.toList());
    }
}
```

## References
- See `examples/` for `GoogleDriveExample.java` and `GmailExample.java`.
- See `references/google-oauth2-setup.md` for project configuration.

