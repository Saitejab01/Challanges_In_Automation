package deepakSirChallanges;

import static io.restassured.RestAssured.given;

import java.util.Random;

import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PrepCRM {
	@Test
	public void createLead() {
		Random rand  = new Random();
		int randNum = rand.nextInt(1000,9999);
		String campaignName="campaignName"+randNum;
		String targetSize="10";

		String campaignJsonBody ="		{\r\n"
				+ "			  \"campaignName\": \""+campaignName+"\",\r\n"
				+ "			  \"targetSize\": "+targetSize+"\r\n"
				+ "			}";
	Response resp=given()
		.contentType(ContentType.JSON)
		.body(campaignJsonBody)
		.when()
		.post("http://49.249.28.218:8098/campaign");
		resp.then()
		.log()
		.all();
		
		String campaignId=resp.jsonPath().get("campaignId");
		
		System.out.println(campaignId);
		String contactName="ContactName"+randNum;
		String mobile = "852314"+randNum;
		
		String contactJsonBody="{\r\n"
				+ "  \"contactName\": \""+contactName+"\",\r\n"
				+ "  \"email\": \""+contactName+"@gmail.com\",\r\n"
				+ "  \"mobile\": \""+mobile+"\"\r\n"
				+ "}";
		Response contactResp = given()
		.contentType(ContentType.JSON)
		.queryParam("campaignId",campaignId )
		.body(contactJsonBody)
		.when()
		.post("http://49.249.28.218:8098/contact");
		contactResp.then()
		.log()
		.all();
		
		String contactID = contactResp.jsonPath().get("contactId");
		System.out.println(contactID);
		
		String company="FireFLink"+randNum;
		String industry="FireFLink industries"+randNum;
		String leadSource="FireFlink lead"+randNum;
		String leadStatus="FireFlink Master";
		String name="Name"+randNum;
		String phone="795648"+randNum;
		String leadJsonBody="{\r\n"
				+ "  \"company\": \""+company+"\",\r\n"
				+ "  \"industry\": \""+industry+"\",\r\n"
				+ "  \"leadSource\": \""+leadSource+"\",\r\n"
				+ "  \"leadStatus\": \""+leadStatus+"\",\r\n"
				+ "  \"name\": \""+name+"\",\r\n"
				+ "  \"phone\": \""+phone+"\"\r\n"
				+ "}";
		
		
		Response leadResp = given()
		.contentType(ContentType.JSON)
		.queryParam("campaignId",campaignId )
		.body(leadJsonBody)
		.when()
		.post("http://49.249.28.218:8098/lead");
		leadResp.then()
		.log()
		.all();
		String leadID=leadResp.jsonPath().get("leadId");
		System.out.println(leadID);
		
		String opportunityJsonBody="{\r\n"
				+ "  \"nextStep\": \"Checking\",\r\n"
				+ "  \"opportunityName\": \"OPPO\",\r\n"
				+ "  \"probability\": \"50\",\r\n"
				+ "  \"salesStage\": \"JaiBavani\"\r\n"
				+ "}";
		
		Response opportunityResp = given()
		.contentType(ContentType.JSON)
		.queryParam("leadId",leadID )
		.queryParam("campaignId",campaignId )
		.body(opportunityJsonBody)
		.when()
		.post("http://49.249.28.218:8098/opportunity");
		opportunityResp.then()
		.log()
		.all();
		String opportunityID=opportunityResp.jsonPath().get("opportunityId");
		System.out.println(opportunityID);
		
		
		
	}
}
