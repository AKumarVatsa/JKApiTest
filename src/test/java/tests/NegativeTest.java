package tests;

import static io.restassured.RestAssured.given;
import static constants.EndPoints.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class NegativeTest extends BaseTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String CONTENT_TYPE = "application/json";
    private Response response;

    private void logAndAssertMessageContains(String expectedMessage) {
        String body = response.getBody().asString();
        System.out.println("Response Body: " + body);
        test.log(Status.INFO, "Response body: " + body);
        String actualMessage = response.jsonPath().getString("message");
        Assert.assertTrue(actualMessage.contains(expectedMessage),
                "Expected message to contain: " + expectedMessage);
    }

    private String buildUserPayload(String username, String password) {
        return String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);
    }

    private String buildBookPayload(String userId, String isbn) {
        return String.format("""
                {
                  "userId": "%s",
                  "collectionOfIsbns": [{ "isbn": "%s" }]
                }
                """, userId, isbn);
    }

    private String buildDeleteOrUpdatePayload(String userId, String isbn) {
        return String.format("""
                {
                  "userId": "%s",
                  "isbn": "%s"
                }
                """, userId, isbn);
    }

    @Test
    public void testCreateUserWithWeakPassword() {
        test = report.createTest("Create User with Weak Password");

        String body = buildUserPayload("weakUser123", "12345");

        response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(CREATE_USER)
                .then()
                .statusCode(400)
                .extract().response();

        logAndAssertMessageContains("Passwords must have");
        test.log(Status.PASS, "Weak password was correctly rejected.");
    }

    @Test
    public void testGenerateTokenInvalidCredentials() {
        test = report.createTest("Generate Token with Invalid Credentials");

        String body = buildUserPayload("nonexistentUser", "wrongPass1!");

        response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(GENERATE_TOKEN)
                .then()
                .statusCode(200)
                .extract().response();

        String status = response.jsonPath().getString("status");
        Assert.assertEquals(status, "Failed", "Expected token generation to fail with invalid credentials.");

        test.log(Status.INFO, "Token generation failed as expected.");
        test.log(Status.PASS, "Invalid login attempt was correctly handled.");
        System.out.println("Response Body: " + response.getBody().asString());
    }

    @Test
    public void createBookInCollection() {
        test = report.createTest("Create Book without Authorization");

        String body = buildBookPayload(AccountsTest.id, ConfigManager.get("firstIsbn"));

        response = given()
                .contentType(CONTENT_TYPE)
                .body(body)
                .when()
                .post(ADD_BOOKS)
                .then()
                .statusCode(401)
                .extract().response();

        logAndAssertMessageContains("User not authorized!");
        test.log(Status.PASS, "Unauthorized book creation was correctly rejected.");
    }

    @Test
    public void testUserIsCreated() {
        test = report.createTest("Get User with Invalid User ID");

        response = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", "invalidUserId")
                .when()
                .get(GET_USER + "{userId}")
                .then()
                .statusCode(401)
                .extract().response();

        logAndAssertMessageContains("User not authorized!");
        test.log(Status.PASS, "Invalid user ID was correctly rejected.");
    }

    @Test(dependsOnMethods = { "tests.AccountsTest.testCreateUser" })
    public void updateBookInCollection() {
        test = report.createTest("Update Book without ISBN");

        String body = buildDeleteOrUpdatePayload(AccountsTest.id, "");

        response = given()
                .header(AUTH_HEADER, "Bearer " + AccountsTest.token)
                .contentType(CONTENT_TYPE)
                .pathParam("isbn", ConfigManager.get("firstIsbn"))
                .body(body)
                .when()
                .put(UPDATE_BOOKS + "{isbn}")
                .then()
                .statusCode(400)
                .extract().response();

        logAndAssertMessageContains("Request Body is Invalid!");
        test.log(Status.PASS, "Update without ISBN was correctly rejected.");
    }

    @Test
    public void deleteBookFromCollection() {
        test = report.createTest("Delete Book without User ID");

        String body = buildDeleteOrUpdatePayload("", ConfigManager.get("firstIsbn"));

        response = given()
                .header(AUTH_HEADER, "Bearer " + AccountsTest.token)
                .contentType(CONTENT_TYPE)
                .body(body)
                .when()
                .delete(DELETE_BOOK)
                .then()
                .statusCode(401)
                .extract().response();

        logAndAssertMessageContains("User Id not correct!");
        test.log(Status.PASS, "Delete without user ID was correctly rejected.");
    }
}
