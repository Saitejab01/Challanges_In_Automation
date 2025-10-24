package zomato;
import jakarta.mail.*;
import jakarta.mail.search.*;
import java.util.*;
import java.io.*;
import java.time.*;

public class GmailOTPFetcher {

    public static String fetchZomatoOtpEmail(String username, String appPassword, int bufferMinutes) {
        String host = "imap.gmail.com";
        String otpEmailHtml = null;

        try {
            // Set up properties
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");

            // Create session
            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore();
            store.connect(host, username, appPassword);

            // Open inbox
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // Current time and buffer
            Instant now = Instant.now();
            Instant bufferTime = now.minus(Duration.ofMinutes(bufferMinutes));

            // Search by subject and received date
            SearchTerm subjectTerm = new SubjectTerm("Zomato OTP");
            SearchTerm recentTerm = new ReceivedDateTerm(ComparisonTerm.GE, Date.from(bufferTime));
            SearchTerm andTerm = new AndTerm(subjectTerm, recentTerm);

            Message[] messages = emailFolder.search(andTerm);

            if (messages.length > 0) {
                Message message = messages[messages.length - 1]; // latest
                Object content = message.getContent();

                if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (part.isMimeType("text/html")) {
                            otpEmailHtml = (String) part.getContent();
                            break;
                        }
                    }
                } else if (content instanceof String && message.isMimeType("text/html")) {
                    otpEmailHtml = (String) content;
                }
            }

            emailFolder.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return otpEmailHtml;
    }
    public static void main(String[] args) {
    	String email = "saitejab01@gmail.com";
        String appPassword = "9866969783";
        fetchZomatoOtpEmail(email, appPassword, 5);
	}
}
