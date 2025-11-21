package practice;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

public class preethi {
	@SuppressWarnings("deprecation")
	@Test
	public void main() throws MalformedURLException {
		EdgeOptions options3 = new EdgeOptions();
		RemoteWebDriver driver = new  RemoteWebDriver(new URL("http://192.168.1.170:4444"),options3);
		driver.get("https://youtube.com");
		System.out.println(driver.getPageSource());
	}
}
