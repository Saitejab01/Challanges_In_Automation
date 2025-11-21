package deepakSirChallanges;


import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.windows.WindowsDriver;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ERP_Testing_E2E {
	
	private static Process appiumProcess;
	@SuppressWarnings("unused")
	private static Process emulatorProcess;

	ExcelUtils ex = new ExcelUtils(".\\src\\test\\resources\\ERP_E2E.xlsx","Master");
	Faker f = new Faker();
	
	@Test (priority = 1)
	public void inventory() throws Throwable {
		
		//Inventory Management
		
		WebDriver d = new FirefoxDriver();
		d.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
		d.manage().window().maximize();
		d.get("http://49.249.28.218:8097");
		d.findElement(By.id("username")).sendKeys("rmgyantra");
		d.findElement(By.id("inputPassword")).sendKeys("rmgy@9999");
		d.findElement(By.xpath("//button[.='Login']")).click();
		
		
		//Adding Vendor
		d.findElement(By.xpath("//a[.='Vendors']")).click();
		d.findElement(By.xpath("//span[.='Add New Vendor']")).click();
		
		String Vendor_Name = f.name().firstName();
		ex.setCellDataByHeader(1, "Vendor_Name", Vendor_Name);

		
		d.findElement(By.xpath("//label[contains(.,'Name')]/span/../../input")).sendKeys(Vendor_Name);
		d.findElement(By.xpath("//label[contains(.,'Email')]/span/../../input")).sendKeys(Vendor_Name + "@gmail.com");
		d.findElement(By.xpath("//label[contains(.,'Phone')]/span/../../input")).sendKeys(f.number().digits(10));
		
		Select categoryEle = new Select(d.findElement(By.xpath("//option[.='Select Category']/..")));
		String Vendor_Category = "Electronics";
		categoryEle.selectByVisibleText(Vendor_Category);
		ex.setCellDataByHeader(1, "Vendor_Category", Vendor_Category);
		
		WebElement add = d.findElement(By.xpath("//input[@value=\"Add\"]"));
		add.click();
		WebElement popUp = d.findElement(By.xpath("//div[contains(text(),'Successfully Created')]"));
		WebDriverWait w = new WebDriverWait(d, Duration.ofSeconds(10));
		w.until(ExpectedConditions.visibilityOf(popUp));
		System.out.println("=========== Vendor Created =============");
	
		
		//Adding Product
		d.findElement(By.xpath("//a[.='Products']")).click();
		d.findElement(By.xpath("//span[.='Create Product']")).click();
		String Product_Name = f.commerce().productName();
		d.findElement(By.xpath("//input[@name=\"productName\"]")).sendKeys(Product_Name);
		ex.setCellDataByHeader(1, "Product_Name", Product_Name);
		
		Select prodCat = new Select(d.findElement(By.xpath("//select[@name=\"productCategory\"]")));
		String Product_Category = "Electronics";
		prodCat.selectByVisibleText(Product_Category);
		ex.setCellDataByHeader(1, "Product_Category", Product_Category);
		
		d.findElement(By.xpath("//input[@name=\"quantity\"]")).sendKeys("99");
		d.findElement(By.xpath("//input[@name='price']")).sendKeys(f.number().digits(3));
				
		
		Select prodVendor = new Select(d.findElement(By.xpath("//select[@name=\"vendorId\"]")));
		prodVendor.selectByVisibleText(Vendor_Name + " - (Electronics)");
		d.findElement(By.xpath("//input[@value='Add product']")).click();
		popUp = d.findElement(By.xpath("//div[contains(text(),'Successfully Added')]"));
		w.until(ExpectedConditions.visibilityOf(popUp));
		System.out.println("=========== Product Created =============");
		
		Select searchProd = new Select(d.findElement(By.xpath("//option[@value=\"productId\"]/..")));
		searchProd.selectByVisibleText("Search by Product Name");
		d.findElement(By.xpath("//input[@placeholder=\"Search by product Name\"]")).sendKeys(Product_Name);
		String Product_Id = d.findElement(By.xpath("//td[contains(text(),'PROD')]")).getText();
		ex.setCellDataByHeader(1, "Product_Id", Product_Id);
		System.out.println("Product_Id :" +Product_Id);
		d.quit();
	}
		
		//CRM

		@Test (priority = 2)
		public void crm() {
		
		RestAssured.baseURI = "http://49.249.28.218:8098";
		
		//Adding Campaign
		
		String Campaign_Name = "Campaign_" + f.number().digits(5);
		String Target_Size = f.number().digit();
		
		String campaignBody = "{\r\n"
				+ "  \"campaignName\": \""+Campaign_Name+"\",\r\n"
				+ "  \"targetSize\": "+Target_Size+"\r\n"
				+ "}";
		
		Response resp = given()
			.contentType("application/json")
			.body(campaignBody)
		.when()
			.post("/campaign");
		resp.then().statusCode(201).log().all();
		
		String Campaign_Id = resp.jsonPath().get("campaignId");
		
		ex.setCellDataByHeader(1, "Campaign_Id", Campaign_Id);
		ex.setCellDataByHeader(1, "Campaign_Name", Campaign_Name);
		ex.setCellDataByHeader(1, "Target_Size", Target_Size);
		
		System.out.println("========= Campaign Created =========== ");
		
		//Adding Contact
		String Contact_Name = f.name().firstName();
		String mobile = f.number().digits(10);
		String email = Contact_Name + "@gmail.com";
		
		String contactBody = "{\r\n"
				+ "  \"contactName\": \""+Contact_Name+"\", \r\n"
				+ "  \"mobile\": \""+mobile+"\",\r\n"
				+ "  \"email\":\""+email+"\"\r\n"
				+ "}";
		
		resp = given()
		 	.contentType("application/json")
		 	.queryParam("campaignId", Campaign_Id)
		 	.body(contactBody)
		.when()
			.post("/contact");
		resp.then().statusCode(201)
			.log().all();
		
		String contactId = resp.jsonPath().get("contactId");
		System.out.println("Contact Id : "+contactId);
		
		ex.setCellDataByHeader(1, "Contact_Name", Contact_Name);
		System.out.println("========= Contact Created =========== ");
		
		//Adding Lead
		String Company_Name = f.company().name();
		String industry = "Electrical" + f.number().digits(3);
		String leadSource = "source"+ f.number().digits(3);
		String Lead_Status = "source status";
		String Lead_Name = f.name().fullName();
		String phone = f.number().digits(10);
		
		String leadBody = "{\r\n"
                + "  \"company\": \""+Company_Name+"\",\r\n"
                + "  \"industry\": \""+industry+"\",\r\n"
                + "  \"leadSource\": \""+leadSource+"\",\r\n"
                + "  \"leadStatus\": \""+Lead_Status+"\",\r\n"
                + "  \"name\": \""+Lead_Name+"\",\r\n"
                + "  \"phone\": \""+phone+"\"\r\n"
                + "}";
		
		resp = given()
			.contentType("application/json")
		 	.queryParam("campaignId", Campaign_Id)
		 	.body(leadBody)
		.when()
			.post("/lead");
		resp.then()
			.statusCode(201).log().all();
		
		String leadId = resp.jsonPath().get("leadId");
		ex.setCellDataByHeader(1, "Lead_Name", Lead_Name);
		ex.setCellDataByHeader(1, "Lead_Status", Lead_Status);
		ex.setCellDataByHeader(1, "Company_Name", Company_Name);
		System.out.println("========= Lead Created =========== ");
		
		//Adding Opportunity
		String Opportunity_Name = f.commerce().department();
		
		String opportunityBody = "{"
				+ "\"opportunityName\": \""+Opportunity_Name+"\"}";
		
		resp = given()
			.contentType("application/json")
		 	.queryParam("leadId", leadId)
		 	.body(opportunityBody)
	 	.when()
	 		.post("/opportunity");
		resp.then()
			.statusCode(201)
			.log().all();
		
		String opportunityId = resp.jsonPath().get("opportunityId");
		ex.setCellDataByHeader(1, "Opportunity_Name", Opportunity_Name);
		System.out.println("========= Opportunity Created =========== ");
		
		//Adding Quote
		
		String Product_Name =ex.getCellDataByHeader(1, "Product_Name");
		String Product_Id = ex.getCellDataByHeader(1, "Product_Id");
		
		String quoteBody = "{\r\n"
				+ "    \"opportunityId\": \""+opportunityId+"\",\r\n"
				+ "    \"contactId\": \""+contactId+"\",\r\n"
				+ "    \"quotes\": {\r\n"
				+ "        \"quoteId\": \"\",\r\n"
				+ "        \"quoteStage\": \"sfd\",\r\n"
				+ "        \"netTotal\": 99,\r\n"
				+ "        \"shippingAndHandlingCharges\": \"\",\r\n"
				+ "        \"discount\": \"\",\r\n"
				+ "        \"grandTotal\": 99\r\n"
				+ "    },\r\n"
				+ "    \"products\": [\r\n"
				+ "        {\r\n"
				+ "            \"productId\": \""+Product_Id+"\",\r\n"
				+ "            \"productName\": \""+Product_Name+"\",\r\n"
				+ "            \"price\": \"99\"\r\n"
				+ "        }\r\n"
				+ "    ],\r\n"
				+ "    \"billingAddress\": {\r\n"
				+ "        \"address\": \"Jayanagar\",\r\n"
				+ "        \"poBox\": \"Jayanagar\",\r\n"
				+ "        \"city\": \"Bangalore\",\r\n"
				+ "        \"state\": \"Karnataka\",\r\n"
				+ "        \"postalCode\": \"560007\",\r\n"
				+ "        \"country\": \"India\"\r\n"
				+ "    },\r\n"
				+ "    \"shippingAddress\": {\r\n"
				+ "        \"address\": \"Jayanagar\",\r\n"
				+ "        \"poBox\": \"Jayanagar\",\r\n"
				+ "        \"city\": \"bangalore\",\r\n"
				+ "        \"state\": \"Karnataka\",\r\n"
				+ "        \"postalCode\": \"560007\",\r\n"
				+ "        \"country\": \"India\"\r\n"
				+ "    },\r\n"
				+ "    \"productQuantities\": {\r\n"
				+ "        \""+Product_Id+"\": 3\r\n"
				+ "    }\r\n"
				+ "}\r\n"
				+ "";
		
		resp=given()
			.contentType("application/json")
			.body(quoteBody)
		.when()
			.post("/quote");
		resp.then()
			.statusCode(201).log().all();
		String quoteId = resp.jsonPath().get("quoteId");
		System.out.println("============ Quote Created ====================");
		
		
		//Adding Sales Order
		String subject = "SalesOrder" + f.number().digits(3);
		String validTill = f.date().toString();
		
		String salesBody = "{\r\n"
				+ "    \"opportunityId\": \""+opportunityId+"\",\r\n"
				+ "    \"contactId\": \""+contactId+"\",\r\n"
				+ "    \"quoteId\": \""+quoteId+"\",\r\n"
				+ "    \"salesOrder\": {\r\n"
				+ "        \"quoteId\": \""+quoteId+"\",\r\n"
				+ "        \"subject\": \""+subject+"\",\r\n"
				+ "        \"validTill\": \""+validTill+"\",\r\n"
				+ "        \"status\": \"InProgress\",\r\n"
				+ "        \"netTotal\": 99,\r\n"
				+ "        \"shippingAndHandlingCharges\": \"\",\r\n"
				+ "        \"discount\": \"\",\r\n"
				+ "        \"grandTotal\": 99\r\n"
				+ "    },\r\n"
				+ "    \"products\": [\r\n"
				+ "        {\r\n"
				+ "            \"productId\": \""+Product_Id+"\",\r\n"
				+ "            \"productName\": \""+Product_Name+"\",\r\n"
				+ "            \"price\": \"99\"\r\n"
				+ "        }\r\n"
				+ "    ],\r\n"
				+ "    \"billingAddress\": {\r\n"
				+ "        \"address\": \"Kolar\",\r\n"
				+ "        \"poBox\": \"Jayanagar\",\r\n"
				+ "        \"city\": \"Kolar\",\r\n"
				+ "        \"state\": \"Karnataka\",\r\n"
				+ "        \"postalCode\": \"560085\",\r\n"
				+ "        \"country\": \"India\"\r\n"
				+ "    },\r\n"
				+ "    \"shippingAddress\": {\r\n"
				+ "        \"address\": \"Kolar\",\r\n"
				+ "        \"poBox\": \"Jayanagar\",\r\n"
				+ "        \"city\": \"Kolar\",\r\n"
				+ "        \"state\": \"Karnataka\",\r\n"
				+ "        \"postalCode\": \"560085\",\r\n"
				+ "        \"country\": \"India\"\r\n"
				+ "    },\r\n"
				+ "    \"productQuantities\": {\r\n"
				+ "        \""+Product_Id+"\": 1\r\n"
				+ "    }\r\n"
				+ "}";
		resp=given()
				.contentType("application/json")
				.body(salesBody)
			.when()
				.post("/sales-order");
			resp.then()
				.statusCode(201).log().all();
			String salesOrderId = resp.jsonPath().get("orderId");
			String Address_Id = resp.jsonPath().get("shippingAddress.addressId");
			ex.setCellDataByHeader(1, "Subject", subject);
		System.out.println("================= Sales Order Created ===============");
		
		//Adding Invoice	
		String invoiceBody ="{\r\n"
                + "  \"billingAddress\": {\r\n"
                + "    \"addressId\": \""+Address_Id+"\"\r\n"
                + "  },\r\n"
                + "  \"contactId\": \""+contactId+"\",\r\n"
                + "  \"invoice\": {\r\n"
                + "    \"billingAddress\": {\r\n"
                + "      \"addressId\": \""+Address_Id+"\"\r\n"
                + "    },\r\n"
                + "    \"contact\": {\r\n"
                + "      \"campaign\": {\r\n"
                + "        \"campaignId\": \""+Campaign_Id+"\"\r\n"
                + "      },\r\n"
                + "      \"contactId\": \""+contactId+"\"\r\n"
                + "    },\r\n"
                + "    \"invoiceId\": \"\",\r\n"
                + "    \"products\": [\r\n"
                + "      {\r\n"
                + "        \"productId\": \""+Product_Id+"\"\r\n"
                + "      }\r\n"
                + "    ],\r\n"
                + "    \"salesOrder\": {\r\n"
                + "      \"billingAddress\": {\r\n"
                + "        \"addressId\": \""+Address_Id+"\"\r\n"
                + "      },\r\n"
                + "      \"contact\": {\r\n"
                + "        \"campaign\": {\r\n"
                + "          \"campaignId\": \""+Campaign_Id+"\"\r\n"
                + "        },\r\n"
                + "        \"contactId\": \""+contactId+"\"\r\n"
                + "      },\r\n"
                + "      \"opportunity\": {\r\n"
                + "        \"lead\": {\r\n"
                + "          \"campaign\": {\r\n"
                + "            \"campaignId\": \""+Campaign_Id+"\"\r\n"
                + "          },\r\n"
                + "          \"leadId\": \""+leadId+"\"\r\n"
                + "        },\r\n"
                + "        \"opportunityId\": \""+opportunityId+"\"\r\n"
                + "      },\r\n"
                + "      \"orderId\": \""+salesOrderId+"\",\r\n"
                + "      \"products\": [\r\n"
                + "        {\r\n"
                + "          \"productId\": \""+Product_Id+"\"\r\n"
                + "        }\r\n"
                + "      ],\r\n"
                + "      \"quote\": {\r\n"
                + "        \"billingAddress\": {\r\n"
                + "          \"addressId\": \""+Address_Id+"\"\r\n"
                + "        },\r\n"
                + "        \"contact\": {\r\n"
                + "          \"campaign\": {\r\n"
                + "            \"campaignId\": \""+Campaign_Id+"\"\r\n"
                + "          },\r\n"
                + "          \"contactId\": \""+contactId+"\"\r\n"
                + "        },\r\n"
                + "        \"opportunity\": {\r\n"
                + "          \"lead\": {\r\n"
                + "            \"campaign\": {\r\n"
                + "              \"campaignId\": \""+Campaign_Id+"\"\r\n"
                + "            },\r\n"
                + "            \"leadId\": \""+leadId+"\"\r\n"
                + "          },\r\n"
                + "          \"opportunityId\": \""+opportunityId+"\"\r\n"
                + "        },\r\n"
                + "        \"products\": [\r\n"
                + "          {\r\n"
                + "            \"productId\": \""+Product_Id+"\"\r\n"
                + "          }\r\n"
                + "        ],\r\n"
                + "        \"quoteId\": \""+quoteId+"\",\r\n"
                + "        \"shippingAddress\": {\r\n"
                + "          \"addressId\": \""+Address_Id+"\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"shippingAddress\": {\r\n"
                + "        \"addressId\": \""+Address_Id+"\"\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    \"shippingAddress\": {\r\n"
                + "      \"addressId\": \""+Address_Id+"\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"orderId\": \""+salesOrderId+"\",\r\n"
                + "  \"productQuantities\": {\r\n"
                + "    \""+Product_Id+"\": 1\r\n"
                + "  },\r\n"
                + "  \"products\": [\r\n"
                + "    {\r\n"
                + "      \"productId\": \""+Product_Id+"\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"shippingAddress\": {\r\n"
                + "    \"addressId\": \""+Address_Id+"\"\r\n"
                + "  }\r\n"
                + "}";
		resp=given()
				.contentType("application/json")
				.body(invoiceBody)
			.when()
				.post("/invoice");
			resp.then()
				.statusCode(201).log().all();
		String invoiceId = resp.jsonPath().get("invoiceId");
		ex.setCellDataByHeader(1, "invoiceId", invoiceId);
		System.out.println("==============Invoice Created===============");
	}
		
	@Test (priority = 4)
	public void logisticMobile() throws MalformedURLException {
		String invoiceId = ex.getCellDataByHeader(1, "invoiceId");
		System.out.println(invoiceId);
		
		UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setDeviceName("emulator-5554");
        options.setAutomationName("UiAutomator2");
        options.setAppPackage("com.acoe.logistics_app");
        options.setAppActivity("com.acoe.logistics_app.MainActivity");
        options.setNoReset(true);
        options.setCapability("appium:forceAppLaunch", true);

        @SuppressWarnings("deprecation")
		AndroidDriver driver = new AndroidDriver(
            new URL("http://127.0.0.1:4724"), options);
        
       driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        
       driver.findElement(By.id("com.acoe.logistics_app:id/usernameEditText")).sendKeys("rmgyantra");
        
       driver.findElement(By.id("com.acoe.logistics_app:id/passwordEditText")).sendKeys("rmgy@9999");
       driver.findElement(By.id("com.acoe.logistics_app:id/loginButton")).click();     
       boolean homeHeading = driver.findElement(By.xpath("//android.widget.TextView[contains(@class,\"android.widget.TextView\") and normalize-space(@text)=\"LogiTrack\"]")).isDisplayed();
       Assert.assertTrue(homeHeading);    
       driver.findElement(By.id("com.acoe.logistics_app:id/searchInput")).sendKeys(invoiceId);  
       String actualText = driver.findElement(By.id("com.acoe.logistics_app:id/invoice_id")).getText();
       Assert.assertEquals(actualText, invoiceId);
       
	}	
	@SuppressWarnings("deprecation")
	@Test (priority = 3)
    public void  brm() throws Throwable{
		
        WindowsDriver driver = null;
        String invoiceId = ex.getCellDataByHeader(1, "invoiceId");

        try {
            // 1. Set Desired Capabilities
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("platformName", "Windows");
            caps.setCapability("deviceName", "WindowsPC");
            caps.setCapability("app", "C:\\Program Files\\brm-app\\brm-app.exe");
            caps.setCapability("automationName", "Windows");
            // Optional: increase command timeout
            caps.setCapability("newCommandTimeout", 300);

            // 2. Connect to WinAppDriver
            
            driver = new WindowsDriver(new URL("http://127.0.0.1:4724/"), caps);

            Thread.sleep(7000);
            
            WebElement username = driver.findElement(AppiumBy.accessibilityId("username"));
            username.clear();
            Thread.sleep(1000);
            
            for (char c : "rmgyantra".toCharArray()) {
                username.sendKeys(String.valueOf(c));
                Thread.sleep(50);
            }
            
            WebElement passwordField = driver.findElement(AppiumBy.accessibilityId("password"));
            passwordField.clear();
            passwordField.click();
            for (char c : "rmgy@9999".toCharArray()) {
                passwordField.sendKeys(String.valueOf(c));
                Thread.sleep(50);
            }
            
            driver.findElement(AppiumBy.accessibilityId("loginButton")).click();
            
            Thread.sleep(4000);
            
            driver.findElement(AppiumBy.name("Invoices")).click();
            Thread.sleep(3000);
            driver.findElement(AppiumBy.accessibilityId("refreshInvoices")).click();
            Thread.sleep(3000);
            driver.findElement(By.name(invoiceId)).isDisplayed();
            String text= driver.findElement(By.name(invoiceId)).getText();
            
            System.out.println(text);
            System.out.println("==== BRM Verified =====");

        } finally {

            if(driver != null) {
                driver.quit();
            }
        }
    }
	
	@BeforeSuite
	public void startEmulatorAndServer() throws Exception {
	    System.out.println("🚀 Starting Emulator and Appium Server...");

	    String emulatorCmd = "C:\\Users\\User\\AppData\\Local\\Android\\Sdk\\emulator\\emulator.exe -avd Pixel_5 -no-snapshot-load -no-snapshot-save -no-boot-anim";
	    emulatorProcess = runCommandAndReturnProcess(emulatorCmd);
	    System.out.println("📱 Emulator launched... waiting for boot...");

	    waitForEmulatorBoot();
	    System.out.println("✅ Emulator booted successfully!");

	    String appiumCmd = "appium --port 4724";
	    appiumProcess = runCommandAndReturnProcess(appiumCmd);
	    System.out.println("✅ Appium server started on port 4724");
	}
	@SuppressWarnings("deprecation")
	@AfterSuite
	public void stopEmulatorAndServer() throws Exception {
	    System.out.println("🛑 Stopping Appium Server and Emulator...");

	    if (appiumProcess != null && appiumProcess.isAlive()) {
	        appiumProcess.destroy();
	        System.out.println("✅ Appium server stopped.");
	    } else {
	        System.out.println("⚠️ Appium process not found or already stopped.");
	    }

	    Runtime.getRuntime().exec("adb -s emulator-5554 emu kill");
	    System.out.println("✅ Emulator stopped.");
	}
	
	public static Process runCommandAndReturnProcess(String command) throws IOException {
	    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
	    builder.redirectErrorStream(true);
	    Process process = builder.start();

	    // Print logs asynchronously
	    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    new Thread(() -> {
	        try {
	            String line;
	            while ((line = reader.readLine()) != null)
	                System.out.println(line);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }).start();

	    return process;
	}

	public static void waitForEmulatorBoot() throws IOException, InterruptedException {
	    boolean booted = false;
	    while (!booted) {
	        @SuppressWarnings("deprecation")
			Process process = Runtime.getRuntime().exec("adb shell getprop sys.boot_completed");
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line = reader.readLine();
	        if (line != null && line.trim().equals("1")) {
	            booted = true;
	        } else {
	            System.out.println("⏳ Emulator booting...");
	            Thread.sleep(5000);
	        }
	    }
	}

}
