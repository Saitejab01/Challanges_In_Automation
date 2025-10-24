package amazon;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class MultiLingual {
	public static void main(String[] args)throws Throwable {
		WebDriver driver = new EdgeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		driver.get("https://www.amazon.in/");
//		File file = new File("C:\\Users\\User\\eclipse-workspace\\Fireflink.Zomato.Automation\\Test_Data\\English.xlsx");
		File file = new File("C:\\Users\\User\\eclipse-workspace\\Fireflink.Zomato.Automation\\Test_Data\\Telugu.xlsx");
		Workbook wb = WorkbookFactory.create(file);
		Sheet sh = wb.getSheet("Sheet1");
		Row row = sh.getRow(1);
		try {
			driver.findElement(By.xpath("//button[@alt='Continue shopping']")).click();
		} catch (Exception e) {
		}
		Actions act = new Actions(driver);
		act.moveToElement(driver.findElement(By.xpath("//button[@aria-label='Expand to Change Language or Country']"))).perform();
		String lang = row.getCell(3).toString();
		System.out.println(lang);
		Thread.sleep(2000);
		WebElement langEle = driver.findElement(By.xpath("//span[.='"+lang+"']"));
		langEle.click();
		String searchText = row.getCell(2).toString();
		System.out.println(searchText);
		Thread.sleep(2000);
		WebElement searchTextTF = driver.findElement(By.xpath("//input[@placeholder=\""+searchText+"\"]"));
		searchTextTF.sendKeys("mobiles");
		driver.findElement(By.id("nav-search-submit-button")).click();
		Thread.sleep(2000);
		String addBtnText = row.getCell(4).toString();
		System.out.println(addBtnText);
		driver.findElement(By.xpath("//button[.='"+addBtnText+"']")).click();
		driver.quit();
	}
}
