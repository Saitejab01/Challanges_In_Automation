package programElements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
public class OpenDebuggerBrowser1 {

	public static void main(String[] args) throws
	InterruptedException {
        String userProfile = "C:\\ChromeProfiles\\TempChrome";
        int debugPort = 9020;
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-debugging-port=" + debugPort);
        options.addArguments("--user-data-dir=" + userProfile);
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://gmail.google.com");
	}
}
