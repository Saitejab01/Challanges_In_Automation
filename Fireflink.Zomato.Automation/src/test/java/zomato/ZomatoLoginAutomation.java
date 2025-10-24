package zomato;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ZomatoLoginAutomation {

    public static void main(String[] args) {
        // Set up Selenium
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://www.zomato.com/");

            // Simulate login steps via Gmail (e.g., click Gmail button, etc.)
            // Wait for OTP screen to appear...

            // Fetch OTP email
            String email = "saitejab01@gmail.com";
            String appPassword = "9866969783"; // Use app password
            String emailHtml = GmailOTPFetcher.fetchZomatoOtpEmail(email, appPassword, 5); // 5 mins buffer

            if (emailHtml != null) {
                System.out.println("OTP Email (HTML):\n" + emailHtml);
                // You can parse the HTML here to extract the actual OTP using regex or Jsoup
            } else {
                System.out.println("No recent OTP email found.");
            }

        } finally {
            driver.quit();
        }
    }
}
