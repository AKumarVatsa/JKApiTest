package tests;

import static constants.EndPoints.*;
import static io.restassured.RestAssured.given;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.response.Response;

public class BooksTest extends BaseTest {

	public Response response;
	public static String isbnNumber;
	public static String bookTitle;

	@Test(priority = 1, dependsOnMethods = { "tests.AccountsTest.testCreateUser",
			"tests.AccountsTest.testGenerateToken" })
	public void createBookInCollection() {

		test = report.createTest("Create Book");

		String requestBody = String.format("""
				{
				  "userId": "%s",
				  "collectionOfIsbns": [{ "isbn": "%s" }]
				}
				""", AccountsTest.id, ConfigManager.get("firstIsbn"));

		response = given()
				.header("Authorization", "Bearer " + AccountsTest.token)
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post(ADD_BOOKS)
				.then()
				.statusCode(201)
				.extract().response();

		System.out.println("Response Body for create book: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for create book is " + response.getBody().asString());

		isbnNumber = response.jsonPath().getString("books[0].isbn");
		Assert.assertNotNull(isbnNumber);
		test.log(Status.PASS, "User created with ISBN: " + isbnNumber);
	}

	@Test(priority = 2, dependsOnMethods = "createBookInCollection")
	public void testGetAllBooks() {

		test = report.createTest("Get Books");

		response = given()
				.when()
				.get(GET_BOOKS)
				.then()
				.statusCode(200)
				.extract().response();

		System.out.println("Response Body for checking if book created: " + response.getBody().asString()); // helpful
																											// debug

		// print in reports.
		test.log(Status.INFO, "Response body for getting the newly created book is " + response.getBody().asString());

		bookTitle = response.jsonPath().getString("books[0].title");
		Assert.assertNotNull(bookTitle);
		test.log(Status.PASS, "User present with title: " + bookTitle);
	}

	@Test(priority = 3, dependsOnMethods = { "createBookInCollection" })
	public void updateBookInCollection() {

		test = report.createTest("Update Book not supported hence 400");

		String body = String.format("""
				{
				  "userId": "%s",
				  "isbn": "%s"
				}
				""", AccountsTest.id, BooksTest.isbnNumber);

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
		
		System.out.println("Response Body for update books: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for updating book is " + response.getBody().asString());
	}

	@Test(priority = 4, dependsOnMethods = { "createBookInCollection" })
	public void deleteBookFromCollection() {

		test = report.createTest("Delete Book");

		// Log token to verify token is set correctly
		System.out.println("Token used: " + AccountsTest.token);
		Assert.assertNotNull(AccountsTest.token, "Token is null!");

		// Log user id to verify it's set correctly
		System.out.println("User Id used: " + AccountsTest.id);
		Assert.assertNotNull(AccountsTest.id, "Id is null!");
		
		String accountId = AccountsTest.id;

		String body = String.format("""
				{
				  "userId": "%s",
					"isbn": "%s"
				}
				""", AccountsTest.id, BooksTest.isbnNumber);

		response = given()
				.header("Authorization", "Bearer " + AccountsTest.token)
				.contentType("application/json")
				.body(body)
				.when()
				.delete(DELETE_BOOK)
				.then()
				.statusCode(204)
				.extract().response();

		System.out.println("Response Body for delete book: " + response.getBody().asString()); // helpful debug

		// print in reports.
		test.log(Status.INFO, "Response body for deleted book is " + response.getBody().asString());

		String respbody = response.getBody().asString();
		Assert.assertTrue(respbody == null || respbody.trim().isEmpty(), "Expected empty response body");

		System.out.println("DELETE response is empty as expected.");
		test.log(Status.INFO, "DELETE response is empty as expected.");
	}

	@Test(priority = 5, dependsOnMethods = "deleteBookFromCollection")
	public void checkIfBookDeleted() {

	    test = report.createTest("Check if Book is Deleted");

	    // Validate prerequisites
	    Assert.assertNotNull(AccountsTest.token, "Token is null!");
	    Assert.assertNotNull(AccountsTest.id, "User ID is null!");
	    Assert.assertNotNull(BooksTest.isbnNumber, "ISBN is null!");

	    // Send GET request to fetch user collection
	    response = given()
	        .header("Authorization", "Bearer " + AccountsTest.token)
	        .when()
	        .get(GET_USER + AccountsTest.id)
	        .then()
	        .statusCode(200)
	        .extract()
	        .response();

	    String responseBody = response.getBody().asString();
	    System.out.println("User collection after deletion: " + responseBody);
	    test.log(Status.INFO, "User collection after delete: " + responseBody);

	    // Verify the ISBN is no longer in the user's collection
	    Assert.assertFalse(responseBody.contains(BooksTest.isbnNumber),
	        "Deleted book still appears in the user's collection.");

	    test.log(Status.PASS, "Deleted book is no longer in the user's collection.");
	}

	

}
