package practice;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.util.Random;

import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class CRM_SYS_Testing1 {
	@Test
	public void crmSysTesting() throws Throwable{
			Random rand  = new Random();
			int randNum = rand.nextInt(1000,9999);
			String venderName ="Lokesh gangineni"+randNum;
			String productName ="Lokesh"+"Product"+randNum;
			
			

			
			// Campaign Creation
			
			baseURI="http://49.249.28.218:8098";
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
			.post("/campaign");
			
			String campaignId=resp.jsonPath().get("campaignId");
			
			//contact Creation
			String contactName="Lokesh"+randNum;
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
			.post("/contact");
			contactResp.then().log().all();
			
			String contactID = contactResp.jsonPath().get("contactId");
			
			//lead creation
			String company="Lokesh"+randNum;
			String industry="Lokesh industries"+randNum;
			String leadSource="Lokesh lead"+randNum;
			String leadStatus="Lokesh Master";
			String name="Lokesh"+randNum;
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
			.post("/lead");
			leadResp.then().log().all();
			String leadID=leadResp.jsonPath().get("leadId");
			
			
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
			.post("/opportunity");
			opportunityResp.then().log().all();
			String opportunityID=opportunityResp.jsonPath().get("opportunityId");
			
			String quoteJsonBody= "{\r\n"
					+ "    \"opportunityId\": \""+opportunityID+"\",\r\n"
					+ "    \"contactId\": \""+contactID+"\",\r\n"
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
					+ "            \"productId\": \""+"IM_PROD_7525"+"\",\r\n"
					+ "            \"productName\": \""+productName+"\",\r\n"
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
					+ "        \""+"IM_PROD_7525"+"\": 1\r\n"
					+ "    }\r\n"
					+ "}";
			
			Response quoteResp = given()
			.contentType(ContentType.JSON)
			.body(quoteJsonBody)
			.when()
			.post("/quote");
			quoteResp.then().log().all();
			String quoteID=quoteResp.jsonPath().get("quoteId");
			
			
			String purchaseJsonBody="{\r\n"
					+ "    \"contactId\": \""+contactID+"\",\r\n"
					+ "    \"purchaseOrder\": {\r\n"
					+ "        \"orderId\": \"\",\r\n"
					+ "        \"status\": \"Available\",\r\n"
					+ "        \"netTotal\": 99,\r\n"
					+ "        \"shippingAndHandlingCharges\": \"\",\r\n"
					+ "        \"discount\": \"\",\r\n"
					+ "        \"grandTotal\": 99\r\n"
					+ "    },\r\n"
					+ "    \"products\": [\r\n"
					+ "        {\r\n"
					+ "            \"productId\": \""+"IM_PROD_7525"+"\",\r\n"
					+ "            \"productName\": \""+productName+"\",\r\n"
					+ "            \"price\": \"99\"\r\n"
					+ "        }\r\n"
					+ "    ],\r\n"
					+ "    \"billingAddress\": {\r\n"
					+ "        \"address\": \"Jayanagar\",\r\n"
					+ "        \"poBox\": \"Jayanagar\",\r\n"
					+ "        \"city\": \"Kolar\",\r\n"
					+ "        \"state\": \"Karnataka\",\r\n"
					+ "        \"postalCode\": \"560085\",\r\n"
					+ "        \"country\": \"India\"\r\n"
					+ "    },\r\n"
					+ "    \"shippingAddress\": {\r\n"
					+ "        \"address\": \"Jayanagar\",\r\n"
					+ "        \"poBox\": \"Jayanagar\",\r\n"
					+ "        \"city\": \"Kolar\",\r\n"
					+ "        \"state\": \"Karnataka\",\r\n"
					+ "        \"postalCode\": \"560022\",\r\n"
					+ "        \"country\": \"India\"\r\n"
					+ "    },\r\n"
					+ "    \"productQuantities\": {\r\n"
					+ "        \""+"IM_PROD_7525"+"\": 1\r\n"
					+ "    }\r\n"
					+ "}";
			
			Response purchaseResp = given()
			.contentType(ContentType.JSON)
			.body(purchaseJsonBody)
			.when()
			.post("/purchase-order");
			purchaseResp.then().log().all();
			String orderID=purchaseResp.jsonPath().get("orderId");
			
			
			String salesJsonBody="{\r\n"
					+ "    \"opportunityId\": \""+opportunityID+"\",\r\n"
					+ "    \"contactId\": \""+contactID+"\",\r\n"
					+ "    \"quoteId\": \""+quoteID+"\",\r\n"
					+ "    \"salesOrder\": {\r\n"
					+ "        \"quoteId\": \""+quoteID+"\",\r\n"
					+ "        \"status\": \"InProgress\",\r\n"
					+ "        \"netTotal\": 99,\r\n"
					+ "        \"shippingAndHandlingCharges\": \"\",\r\n"
					+ "        \"discount\": \"\",\r\n"
					+ "        \"grandTotal\": 99\r\n"
					+ "    },\r\n"
					+ "    \"products\": [\r\n"
					+ "        {\r\n"
					+ "            \"productId\": \""+"IM_PROD_7525"+"\",\r\n"
					+ "            \"productName\": \""+productName+"\",\r\n"
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
					+ "        \""+"IM_PROD_7525"+"\": 1\r\n"
					+ "    }\r\n"
					+ "}";
			Response salesResp = given()
			.contentType(ContentType.JSON)
			.body(salesJsonBody)
			.when()
			.post("/sales-order");
			salesResp.then().log().all();
			
			String invoiceJsonBody="{\r\n"
					+ "  \"billingAddress\": {\r\n"
					+ "    \"addressId\": \"ADD04770\"\r\n"
					+ "  },\r\n"
					+ "  \"contactId\": \""+contactID+"\",\r\n"
					+ "  \"invoice\": {\r\n"
					+ "    \"billingAddress\": {\r\n"
					+ "      \"addressId\": \"ADD04770\"\r\n"
					+ "    },\r\n"
					+ "    \"contact\": {\r\n"
					+ "      \"campaign\": {\r\n"
					+ "        \"campaignId\": \""+campaignId+"\"\r\n"
					+ "      },\r\n"
					+ "      \"contactId\": \""+contactID+"\"\r\n"
					+ "    },\r\n"
					+ "    \"invoiceId\": \"string\",\r\n"
					+ "    \"products\": [\r\n"
					+ "      {\r\n"
					+ "        \"productId\": \""+"IM_PROD_7525"+"\"\r\n"
					+ "      }\r\n"
					+ "    ],\r\n"
					+ "    \"salesOrder\": {\r\n"
					+ "      \"billingAddress\": {\r\n"
					+ "        \"addressId\": \"ADD04770\"\r\n"
					+ "      },\r\n"
					+ "      \"contact\": {\r\n"
					+ "        \"campaign\": {\r\n"
					+ "          \"campaignId\": \""+campaignId+"\"\r\n"
					+ "        },\r\n"
					+ "        \"contactId\": \""+contactID+"\"\r\n"
					+ "      },\r\n"
					+ "      \"opportunity\": {\r\n"
					+ "        \"lead\": {\r\n"
					+ "          \"campaign\": {\r\n"
					+ "            \"campaignId\": \""+campaignId+"\"\r\n"
					+ "          },\r\n"
					+ "          \"leadId\": \""+leadID+"\"\r\n"
					+ "        },\r\n"
					+ "        \"opportunityId\": \""+opportunityID+"\"\r\n"
					+ "      },\r\n"
					+ "      \"orderId\": \""+orderID+"\",\r\n"
					+ "      \"products\": [\r\n"
					+ "        {\r\n"
					+ "          \"productId\": \""+"IM_PROD_7525"+"\"\r\n"
					+ "        }\r\n"
					+ "      ],\r\n"
					+ "      \"quote\": {\r\n"
					+ "        \"billingAddress\": {\r\n"
					+ "          \"addressId\": \"ADD04770\"\r\n"
					+ "        },\r\n"
					+ "        \"contact\": {\r\n"
					+ "          \"campaign\": {\r\n"
					+ "            \"campaignId\": \""+campaignId+"\"\r\n"
					+ "          },\r\n"
					+ "          \"contactId\": \""+contactID+"\"\r\n"
					+ "        },\r\n"
					+ "        \"opportunity\": {\r\n"
					+ "          \"lead\": {\r\n"
					+ "            \"campaign\": {\r\n"
					+ "              \"campaignId\": \""+campaignId+"\"\r\n"
					+ "            },\r\n"
					+ "            \"leadId\": \""+leadID+"\"\r\n"
					+ "          },\r\n"
					+ "          \"opportunityId\": \""+opportunityID+"\"\r\n"
					+ "        },\r\n"
					+ "        \"products\": [\r\n"
					+ "          {\r\n"
					+ "            \"productId\": \""+"IM_PROD_7525"+"\"\r\n"
					+ "          }\r\n"
					+ "        ],\r\n"
					+ "        \"quoteId\": \""+quoteID+"\",\r\n"
					+ "        \"shippingAddress\": {\r\n"
					+ "          \"addressId\": \"ADD04770\"\r\n"
					+ "        }\r\n"
					+ "      },\r\n"
					+ "      \"shippingAddress\": {\r\n"
					+ "        \"addressId\": \"ADD04770\"\r\n"
					+ "      }\r\n"
					+ "    },\r\n"
					+ "    \"shippingAddress\": {\r\n"
					+ "      \"addressId\": \"ADD04770\"\r\n"
					+ "    }\r\n"
					+ "  },\r\n"
					+ "  \"orderId\": \"SO00597\",\r\n"
					+ "  \"productQuantities\": {\r\n"
					+ "    \""+"IM_PROD_7525"+"\": 1\r\n"
					+ "  },\r\n"
					+ "  \"products\": [\r\n"
					+ "    {\r\n"
					+ "      \"productId\": \""+"IM_PROD_7525"+"\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"shippingAddress\": {\r\n"
					+ "    \"addressId\": \"ADD04770\"\r\n"
					+ "  }\r\n"
					+ "}";
			Response invoiceResp = given()
			.contentType(ContentType.JSON)
			.body(invoiceJsonBody)
			.when()
			.post("/invoice");
			invoiceResp.then().log().all();
		}
		
	}
