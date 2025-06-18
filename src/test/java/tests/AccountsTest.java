package tests;

import static constants.EndPoints.*;
import static io.restassured.RestAssured.given;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class AccountsTest extends BaseTest {

	public static String username = "user" + (int) (Math.random() * 10000);
	public static String token;
	public Response response;
	public static String id;

	@Test(priority = 1)
	public void testCreateUser() {

		test = report.createTest("Create User Test");

		String requestBody = String.format("""
				    {
				      "userName": "%s",
				      "password": "%s"
				    }
				""", username, ConfigManager.get("password"));

		 response = given()
				 .contentType(ContentType.JSON)
				 .body(requestBody)
				 .when()
				 .post(CREATE_USER)
				 .then()
				.statusCode(201)
				.extract().response();

		System.out.println("Response Body for create user: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for new user is " + response.getBody().asString());

		 id = response.jsonPath().getString("userID");
		Assert.assertNotNull(id);
		test.log(Status.PASS, "User created with ID: " + id);
	}

	@Test(priority = 2, dependsOnMethods = "testCreateUser")
	public void testGenerateToken() {
		
		test = report.createTest("Generate Token Test");

		String requestBody = String.format("""
				    {
				      "userName": "%s",
				      "password": "%s"
				    }
				""", username, ConfigManager.get("password"));

		 response = given()
				 .contentType(ContentType.JSON)
				 .body(requestBody)
				 .when()
				 .post(GENERATE_TOKEN)
				.then()
				.statusCode(200)
				.extract().response();

		System.out.println("Response Body for generate token: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for generate token is " + response.getBody().asString());

		token = response.jsonPath().getString("token");
		Assert.assertNotNull(token);
		test.log(Status.PASS, "User token created: " + token);
	}
	
	@Test(priority = 3, dependsOnMethods = "testCreateUser")
	public void testUserIsCreated() {

		test = report.createTest("Check user is created");

		 response = given()
				 .contentType(ContentType.JSON)
				 .header("Authorization", "Bearer " + token)
				 .pathParam("userId", id)
				 .when()
				 .get(GET_USER + "{userId}")
				 .then()
				.statusCode(200)
				.extract().response();

		System.out.println("Response Body to check if user present: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body to check if user is present " + response.getBody().asString());

	}
}
