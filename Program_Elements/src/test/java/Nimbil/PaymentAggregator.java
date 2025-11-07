package Nimbil;

import static io.restassured.RestAssured.*;

import java.time.Duration;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PaymentAggregator {
	public static void main(String[] args) {
		baseURI="https://api.nimbbl.tech";
		String jsonBodyForAuthToken="{\r\n"
				+ "  \"access_key\": \"access_key_pKx7rWVgVpbXQvq2\",\r\n"
				+ "  \"access_secret\": \"access_secret_DX3w55VKAkXbx7aB\"\r\n"
				+ "}";
		System.out.println("********************************Auth_token*********************************");
		Response resp = given()
		.contentType(ContentType.JSON)
		.body(jsonBodyForAuthToken)
		.when()
		.post("/api/v3/generate-token");
		resp.then()
		.log()
		.all();
		String token=resp.jsonPath().get("token");
		
		System.out.println("********************************Order_Creation*********************************");
		
		String invoiceId = "inv_asjjeibdhakk49"+ramdomToken();
		String jsonBodyForOrder ="{\r\n"
				+ "  \"quantity\": 2,\r\n"
				+ "  \"amount_before_tax\": 2100,\r\n"
				+ "  \"tax\": 105,\r\n"
				+ "  \"total_amount\": 2205,\r\n"
				+ "  \"user\": {\r\n"
				+ "    \"email\": \"wonderwoman@themyscira.gov\",\r\n"
				+ "    \"first_name\": \"Diana\",\r\n"
				+ "    \"last_name\": \"Prince\",\r\n"
				+ "    \"country_code\": \"+91\",\r\n"
				+ "    \"mobile_number\": \"9876543210\"\r\n"
				+ "  },\r\n"
				+ "  \"shipping_address\": {\r\n"
				+ "    \"address_1\": \"1080 Beach Mansion\",\r\n"
				+ "    \"street\": \"Magic Beach Drive\",\r\n"
				+ "    \"landmark\": \"Opposite Magic Mountain\",\r\n"
				+ "    \"area\": \"Elyria\",\r\n"
				+ "    \"city\": \"Atlantis\",\r\n"
				+ "    \"state\": \"Castalia\",\r\n"
				+ "    \"pincode\": \"100389\",\r\n"
				+ "    \"address_type\": \"Beach House\",\r\n"
				+ "    \"label\": \"Sunny Home\"\r\n"
				+ "  },\r\n"
				+ "  \"billing_address\": {\r\n"
				+ "    \"address_1\": \"1080 Beach Mansion\",\r\n"
				+ "    \"street\": \"Magic Beach Drive\",\r\n"
				+ "    \"landmark\": \"Opposite Magic Mountain\",\r\n"
				+ "    \"area\": \"Elyria\",\r\n"
				+ "    \"city\": \"Atlantis\",\r\n"
				+ "    \"state\": \"Castalia\",\r\n"
				+ "    \"pincode\": \"100389\",\r\n"
				+ "    \"address_type\": \"Beach House\",\r\n"
				+ "    \"label\": \"Sunny Home\"\r\n"
				+ "  },\r\n"
				+ "  \"currency\": \"INR\",\r\n"
				+ "  \"invoice_id\": \""+invoiceId+"\",\r\n"
				+ "  \"referrer_platform\": \"string\",\r\n"
				+ "  \"referrer_platform_version\": \"string\",\r\n"
				+ "  \"ip_address\": \"106.201.232.161\",\r\n"
				+ "  \"merchant_shopfront_domain\": \"https://merchant-shopfront.example.com\",\r\n"
				+ "  \"offer_enabled\": false,\r\n"
				+ "  \"validate_order_line_item\": false,\r\n"
				+ "  \"order_line_items\": [\r\n"
				+ "    {\r\n"
				+ "      \"sku_id\": \"item_2783027490\",\r\n"
				+ "      \"title\": \"Best Sliced Alphonso Mango\",\r\n"
				+ "      \"description\": \"The Alphonso mango is a seasonal fruit harvested from mid-April through the end of June. The time from flowering to harvest is about 90 days, while the time from harvest to ripening is about 15 days.The fruits generally weigh between 150 and 300 grams (5.3 and 10.6 oz), have a rich, creamy, tender texture and delicate, non-fibrous, juicy pulp. As the fruit matures, the skin of an Alphonso mango turns golden-yellow with a tinge of red across the top of the fruit\",\r\n"
				+ "      \"image_url\": \"https://en.wikipedia.org/wiki/Alphonso_mango#/media/File:Alphonso_mango.jpg\",\r\n"
				+ "      \"rate\": 1050,\r\n"
				+ "      \"quantity\": \"2\",\r\n"
				+ "      \"amount_before_tax\": \"2100.00\",\r\n"
				+ "      \"tax\": \"105.00\",\r\n"
				+ "      \"total_amount\": \"2205.00\",\r\n"
				+ "      \"serial_numbers\": [\r\n"
				+ "        \"359043372654548\",\r\n"
				+ "        \"359043371395481\"\r\n"
				+ "      ]\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"bank_account\": {\r\n"
				+ "    \"account_number\": \"10038849992883\",\r\n"
				+ "    \"name\": \"Diana Prince\",\r\n"
				+ "    \"ifsc\": \"ICIC0000011\"\r\n"
				+ "  },\r\n"
				+ "  \"custom_attributes\": {\r\n"
				+ "    \"name\": \"Diana\",\r\n"
				+ "    \"place\": \"Themyscira\",\r\n"
				+ "    \"animal\": \"Jumpa\",\r\n"
				+ "    \"thing\": \"Tiara\"\r\n"
				+ "  }\r\n"
				+ "}";
		Response resp1 = 
		given()
		.auth()
		.oauth2(token)
		.contentType(ContentType.JSON)
		.body(jsonBodyForOrder)
		.when()
		.post("/api/v3/create-order");
		resp1.then()
		.log()
		.all();
		String userID=resp1.jsonPath().get("user.user_id");
		String orderID=resp1.jsonPath().get("order_id");
		String invoiceID=resp1.jsonPath().get("invoice_id");
		System.out.println(orderID);
		
		System.out.println("********************************Payment*********************************");
		
		
		String jsonBodyForPayment="{\r\n"
				+ "  \"order_id\": \""+orderID+"\",\r\n"
				+ "  \"callback_url\": \"https://mangoseller.awesome/transaction-response\",\r\n"
				+ "  \"payment_mode_code\": \"net_banking\",\r\n"
				+ "  \"bank_code\": \"axis\"\r\n"
				+ "}";
		Response resp2 = given()
				.auth().oauth2(token)
				.contentType(ContentType.JSON)
				.body(jsonBodyForPayment)
		.when()
		.post("/api/v3/initiate-payment");
		resp2.then()
		.log()
		.all();
		String url = resp2.jsonPath().get("next[0].url");
		
		System.out.println("********************************Validation*********************************");
		WebDriver driver = new EdgeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.get(url);
		driver.findElement(By.id("username")).sendKeys("payu");
		driver.findElement(By.id("password")).sendKeys("payu");
		driver.findElement(By.xpath("//input[@type=\"submit\"]")).click();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		WebElement success = wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//label[.='Success']"))));
		System.out.println("success message is visible on ui");
		driver.quit();
	}
	
	
	//------------------------------------------------------
	
	
	public static String ramdomToken() {
    	String token="";
    	String letters ="ABCDEFGHIJKLMNOPQUSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    	Random rand = new Random();
    	for (int i = 0; i < 5; i++) {
    		token+=letters.charAt(rand.nextInt(letters.length()-1));
		}
    	
    	return token;
    }
}
