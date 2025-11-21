package deepakSirChallanges;

import java.io.IOException;

import org.testng.annotations.Test;

public class CRM_CMD {
	@Test
	public void openCmd() throws IOException {
        String filePath = "C:\\Users\\User\\Desktop\\BRM_Jar.jar";
        String invoiceID="INVOICE_00069";
        Runtime.getRuntime().exec("java -jar \"" + filePath + "\" 4040 " + invoiceID);
	}
}
