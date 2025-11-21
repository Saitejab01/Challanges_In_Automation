package deepakSirChallanges;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import io.appium.java_client.windows.WindowsDriver;

public class BRM_Automation {
	@Test
	public void launchApp() throws MalformedURLException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("app", "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App");
        cap.setCapability("platformName", "Windows");
        cap.setCapability("deviceName", "WindowsPC");
		WindowsDriver driver = new WindowsDriver(new URL("http://localhost:4723"),cap);
		System.out.println("calculator launched");
	}
}
