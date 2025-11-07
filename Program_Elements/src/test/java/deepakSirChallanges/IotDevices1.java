package deepakSirChallanges;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;

public class IotDevices1 {
	
	@Test(dataProvider = "getData")
	public void iotAutomation(String jBody) {
		given()
		.contentType(ContentType.JSON)
		.body(jBody)
		.when()
		.post("http://thingsboard.cloud/api/v1/UO7mKGYe0Lik8w7dMzBw/telemetry")
		.then();
	}
	
	@DataProvider(name = "getData")
	public Object[][] getData() throws EncryptedDocumentException, IOException {
        File file = new File("C:\\Users\\User\\git\\AutomationChallanges\\Program_Elements\\src\\test\\resources\\testData.xlsx");
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheet("Sheet1");

        int rowCount = sheet.getPhysicalNumberOfRows(); // total rows (including header)
        int cellCount = sheet.getRow(0).getLastCellNum(); // total columns

        Object[][] data = new Object[rowCount - 1][cellCount];

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < cellCount; j++) {
                Cell cell = row.getCell(j);
                data[i - 1][j] = cell.toString();
            }
        }

        workbook.close();
        return data;
    }
}
