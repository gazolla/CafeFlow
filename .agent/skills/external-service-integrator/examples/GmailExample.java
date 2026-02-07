package io.external.example;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListMessagesResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Example of searching and reading emails using the Gmail SDK.
 */
public class GmailExample {

    public List<Message> searchEmails(Gmail service, String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list("me")
            .setQ(query)
            .execute();

        List<Message> messages = new ArrayList<>();
        if (response.getMessages() != null) {
            for (Message message : response.getMessages()) {
                Message fullMessage = service.users().messages().get("me", message.getId()).execute();
                messages.add(fullMessage);
            }
        }
        return messages;
    }

    public void printSnippet(Gmail service, String messageId) throws IOException {
        Message message = service.users().messages().get("me", messageId).execute();
        System.out.println("Snippet: " + message.getSnippet());
    }
}
