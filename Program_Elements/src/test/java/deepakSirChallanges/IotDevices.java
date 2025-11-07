package deepakSirChallanges;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;

public class IotDevices {
	@Test
	public void iotAutomation() throws EncryptedDocumentException, IOException {
		WebDriver driver = null;
        File file = new File("C:\\Users\\User\\git\\AutomationChallanges\\Program_Elements\\src\\test\\resources\\testData.xlsx");
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheet("Sheet1");
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
        	Row r = sheet.getRow(i+1);
        	for (int j = 0; j < r.getLastCellNum(); j++) {
				Cell cell = r.getCell(j);
				given()
				.contentType(ContentType.JSON)
				.body(cell.toString())
				.when()
				.post("http://thingsboard.cloud/api/v1/UO7mKGYe0Lik8w7dMzBw/telemetry")
				.then();
			}
			
		}
		try {
			baseURI="http://49.249.28.218:8095";
			String jBody="{\r\n"
					+ "\"temperature\": 90\r\n"
					+ "}";
			given()
			.queryParam("id", 2)
			.contentType(ContentType.JSON)
			.body(jBody)
			.when()
			.patch("/api/update-transformer")
			.then();
			System.out.println("90");
			driver = new EdgeDriver();
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			driver.get("http://49.249.28.218:8095/");
			driver.findElement(By.id("username")).sendKeys("rmgyantra");
			driver.findElement(By.id("password")).sendKeys("rmgy@9999");
			driver.findElement(By.xpath("//button[@type='submit']")).click();
			driver.findElement(By.partialLinkText("Transformers")).click();
			driver.findElement(By.xpath("//li[contains(.,'TX-200')]")).click();	
			jBody="{\r\n"
					+ "\"temperature\": 200\r\n"
					+ "}";
			given()
			.queryParam("id", 2)
			.contentType(ContentType.JSON)
			.body(jBody)
			.when()
			.patch("/api/update-transformer")
			.then();
			System.out.println("200");
			driver.navigate().refresh();
			driver.findElement(By.xpath("//li[contains(.,'TX-200')]")).click();	
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			WebElement alert = wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//div[@role=\"alert\"]"))));
			System.out.println("alert element is verified");
			Thread.sleep(5000);
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			driver.quit();	
		}
	}
}
