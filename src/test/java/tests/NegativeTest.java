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

    private Response response;

    private void logAndAssert(String expectedMsg, String actualMsg, String logTitle) {
        Assert.assertTrue(actualMsg.contains(expectedMsg), "Expected message mismatch.");
        test.log(Status.PASS, logTitle);
        test.log(Status.INFO, "Response: " + actualMsg);
    }

    @Test
    public void testCreateUserWithWeakPassword() {
        test = report.createTest("Weak Password");
        String body = """
            { "userName": "weakUser123", "password": "12345" }
            """;

        response = given().contentType(ContentType.JSON).body(body)
                .post(CREATE_USER).then().statusCode(400).extract().response();

        logAndAssert("Passwords must have", response.getBody().asString(), "Weak password rejected");
    }

    @Test
    public void testGenerateTokenInvalidCredentials() {
        test = report.createTest("Invalid Credentials - Generate Token");
        String body = """
            { "userName": "nonexistentUser", "password": "wrongPass1!" }
            """;

        response = given().contentType(ContentType.JSON).body(body)
                .post(GENERATE_TOKEN).then().statusCode(200).extract().response();

        logAndAssert("Failed", response.jsonPath().getString("status"), "Invalid login rejected");
    }

    @Test
    public void createBookInCollection() {
        test = report.createTest("Add Book without Auth");

        String body = String.format("""
            { "userId": "%s", "collectionOfIsbns": [{ "isbn": "%s" }] }
            """, AccountsTest.id, ConfigManager.get("firstIsbn"));

        response = given().contentType("application/json").body(body)
                .post(ADD_BOOKS).then().statusCode(401).extract().response();

        logAndAssert("User not authorized!", response.getBody().asString(), "Unauthorized book creation rejected");
    }

    @Test
    public void testUserIsCreated() {
        test = report.createTest("Get User with Invalid ID");

        response = given().contentType(ContentType.JSON)
                .pathParam("userId", "invalidUserId")
                .get(GET_USER + "{userId}").then().statusCode(401).extract().response();

        logAndAssert("User not authorized!", response.getBody().asString(), "Invalid user fetch rejected");
    }

    @Test(dependsOnMethods = {"tests.AccountsTest.testCreateUser"})
    public void updateBookInCollection() {
        test = report.createTest("Update Book without ISBN");

        String body = String.format("""
            { "userId": "%s", "isbn": "" }
            """, AccountsTest.id);

        response = given()
                .header("Authorization", "Bearer " + AccountsTest.token)
                .contentType("application/json")
                .pathParam("isbn", ConfigManager.get("firstIsbn"))
                .body(body)
                .put(UPDATE_BOOKS + "{isbn}").then().statusCode(400).extract().response();

        logAndAssert("Request Body is Invalid!", response.getBody().asString(), "Update without ISBN rejected");
    }

    @Test
    public void deleteBookFromCollection() {
        test = report.createTest("Delete Book without User ID");

        String body = String.format("""
            { "userId": "", "isbn": "%s" }
            """, ConfigManager.get("firstIsbn"));

        response = given()
                .header("Authorization", "Bearer " + AccountsTest.token)
                .contentType("application/json")
                .body(body)
                .delete(DELETE_BOOK).then().statusCode(401).extract().response();

        logAndAssert("User Id not correct!", response.getBody().asString(), "Delete without user ID rejected");
    }
  
}
