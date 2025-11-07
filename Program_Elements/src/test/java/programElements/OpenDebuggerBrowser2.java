package programElements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class OpenDebuggerBrowser2 {
    public static void main(String[] args) {ChromeOptions options = new ChromeOptions(); 
         options.setExperimentalOption("debuggerAddress", "localhost:9020");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get("https://instagram.com");
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\User\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
         
        
    }
}