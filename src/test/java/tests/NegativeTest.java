package tests;

import static io.restassured.RestAssured.given;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static constants.EndPoints.*;

public class NegativeTest extends BaseTest {
	
	public Response response;

	
	/**
	 * Password doesn't meet complexity requirements (e.g., missing capital letter or special character)
	 */
	
	@Test
	public void testCreateUserWithWeakPassword() {
	    test = report.createTest("Create User with Weak Password");

	    String requestBody = """
	        {
	          "userName": "weakUser123",
	          "password": "12345"
	        }
	        """;

	     response = given()
	        .contentType(ContentType.JSON)
	        .body(requestBody)
	    .when()
	        .post(CREATE_USER)
	    .then()
	        .statusCode(400)
	        .extract().response();

	    String message = response.jsonPath().getString("message");
	    Assert.assertTrue(message.contains("Passwords must have"), "Expected password policy error");
	    test.log(Status.PASS, "Weak password was correctly rejected.");
	    
	    System.out.println("Response Body for create user with weak password: " + response.getBody().asString()); // helpful debug
	   // print in reports.
	 	test.log(Status.INFO, "Response body for new user with weak password is " + response.getBody().asString());
	}
	
	/**
	 * Generate token with invalid credentials.
	 * Generate token gives status as 200 for valid and invalid details.
	 */
	@Test
	public void testGenerateTokenInvalidCredentials() {
	    test = report.createTest("Generate Token with Invalid Credentials");

	    String requestBody = """
	        {
	          "userName": "nonexistentUser",
	          "password": "wrongPass1!"
	        }
	        """;

	     response = given()
	        .contentType(ContentType.JSON)
	        .body(requestBody)
	    .when()
	        .post(GENERATE_TOKEN)
	    .then()
	        .statusCode(200)
	        .extract().response();

	    String status = response.jsonPath().getString("status");
	    Assert.assertEquals(status, "Failed");
	    test.log(Status.PASS, "Invalid login attempt was correctly rejected.");
	    
	    System.out.println("Response Body for generate token with invalid credentials: " + response.getBody().asString()); // helpful debug
		 // print in reports.
		test.log(Status.INFO, "Response body for generate token with invalid credentials " + response.getBody().asString());
	}
	
	
	/**
	 * Create book with no authorisation.
	 * We will get 401 as no authorised.
	 */
	
	@Test
	
	public void createBookInCollection() {

		test = report.createTest("Create Book without authorisation");

		String requestBody = String.format("""
				{
				  "userId": "%s",
				  "collectionOfIsbns": [{ "isbn": "%s" }]
				}
				""", AccountsTest.id, ConfigManager.get("firstIsbn"));

		response = given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post(ADD_BOOKS)
				.then()
				.statusCode(401)
				.extract().response();

		String message = response.jsonPath().getString("message");
	    Assert.assertTrue(message.contains("User not authorized!"));
	    test.log(Status.PASS, "Attempt to create books without authorisation was correctly rejected.");
	    
	    System.out.println("Response Body for create book without authorisation: " + response.getBody().asString()); // helpful debug
		 // print in reports.
		test.log(Status.INFO, "Response body for create book without authorisation " + response.getBody().asString());
	}
	
	/**
	 * Get user but provide wrong user id to search.
	 * We should get 401 not authorised.
	 */
	@Test
	public void testUserIsCreated() {

		test = report.createTest("Get user but search for wrong user id");

		 response = given()
				 .contentType(ContentType.JSON)
				 .pathParam("userId", "invalidUserId")
				 .when()
				 .get(GET_USER + "{userId}")
				 .then()
				.statusCode(401)
				.extract().response();
		 
		 String message = response.jsonPath().getString("message");
		    Assert.assertTrue(message.contains("User not authorized!"));
		    test.log(Status.PASS, "Attempt to get invalid userid was correctly rejected.");
		
		 System.out.println("Response Body for get user with invalid userid: " + response.getBody().asString()); // helpful debug
		 // print in reports.
		test.log(Status.INFO, "Response body for get user with invalid userid " + response.getBody().asString());
}
	
	/**
	 * Update book but dont provide ISBN.
	 */
	@Test(dependsOnMethods = { "tests.AccountsTest.testCreateUser" })
	public void updateBookInCollection() {

		test = report.createTest("Update Book but dont provide ISBN");

		String body = String.format("""
				{
				  "userId": "%s",
				  "isbn": "%s"
				}
				""", AccountsTest.id, "");

		response = given()
				.header("Authorization", "Bearer " + AccountsTest.token)
				.contentType("application/json")
				.pathParam("isbn", ConfigManager.get("firstIsbn"))
				.body(body)
				.when()
				.put(UPDATE_BOOKS + "{isbn}")
				.then()
				.statusCode(400)
				.extract().response();
		
		String message = response.jsonPath().getString("message");
	    Assert.assertTrue(message.contains("Request Body is Invalid!"));
	    test.log(Status.PASS, "Attempt to update book with no ISBN was correctly rejected.");
		
		System.out.println("Response Body for update books with no ISBN: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for updating book with no ISBN " + response.getBody().asString());
	}
	
	/**
	 * Delete book but pass wrong user id.
	 */
	@Test()
	public void deleteBookFromCollection() {

		test = report.createTest("Delete Book but dont provide user id");

		String body = String.format("""
				{
				  "userId": "%s",
					"isbn": "%s"
				}
				""", "", ConfigManager.get("firstIsbn"));

		response = given()
				.header("Authorization", "Bearer " + AccountsTest.token)
				.contentType("application/json")
				.body(body)
				.when()
				.delete(DELETE_BOOK)
				.then()
				.statusCode(401)
				.extract().response();
		
		String message = response.jsonPath().getString("message");
	    Assert.assertTrue(message.contains("User Id not correct!"));
	    test.log(Status.PASS, "Attempt to delete book without passing userid was correctly rejected.");
		
		System.out.println("Response Body for delete books without passing userid: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for delete books without passing userid " + response.getBody().asString());
		
		

		
	}
}
