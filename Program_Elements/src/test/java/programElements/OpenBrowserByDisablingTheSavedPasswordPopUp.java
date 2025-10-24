package programElements;

import java.util.Map;

import org.openqa.selenium.chrome.ChromeOptions;

public class OpenBrowserByDisablingTheSavedPasswordPopUp {

	public static void main(String[] args) {
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", Map.of(
		    "credentials_enable_service", false,
		    "profile.password_manager_enabled", false
		));

	}

}
