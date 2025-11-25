package capchaAutomation;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

public class SliderCaptchaImages {
	public static void main(String[] args) throws Exception {
		WebDriver driver = new EdgeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.get("https://slidercaptcha.com/?utm_source=chatgpt.com");
		driver.findElement(By.xpath("//button[.='Submit']")).click();
		String backgroundImage_id = "background-image";
		String puzzlepiece_id = "puzzle-piece";
		Thread.sleep(2000);
		String backgroundImage = downloadImageByXpath(driver,backgroundImage_id,"C:\\Users\\User\\Desktop\\data\\BI.png");
		String puzzlepieceImage = downloadImageByXpath(driver,puzzlepiece_id,"C:\\Users\\User\\Desktop\\data\\PI.png");
	}



	 public static String downloadImageByXpath(WebDriver driver, String id, String saveToPath) throws Exception {

	        WebElement imgElement = driver.findElement(By.id(id));
	        String imageSrc = imgElement.getAttribute("src");

	        File destination = new File(saveToPath);

	        // Case 1: Normal URL
	        if (imageSrc.startsWith("http")) {
	            URL url = new URL(imageSrc);
	            FileUtils.copyURLToFile(url, destination);
	        }

	        // Case 2: Base64 image
	        else if (imageSrc.startsWith("data:image")) {

	            // Example: data:image/png;base64,XXXX
	            String base64Data = imageSrc.substring(imageSrc.indexOf(",") + 1);

	            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

	            FileUtils.writeByteArrayToFile(destination, imageBytes);
	        }

	        return destination.getAbsolutePath();
	    }
}
