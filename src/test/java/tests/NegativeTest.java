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

public class NegativeTest extends BaseTest {

    private Response response;

    // ─────────────────────────────────────────────
    // 🔧 Utility Methods
    // ─────────────────────────────────────────────

    private Response sendRequest(String method, String endpoint, String body, boolean authRequired, String pathParamKey, String pathParamValue) {
        var request = given().contentType(ContentType.JSON).body(body);

        if (authRequired) {
            request.header("Authorization", "Bearer " + AccountsTest.token);
        }
        if (pathParamKey != null) {
            request.pathParam(pathParamKey, pathParamValue);
        }

        return request.when().request(method, endpoint + (pathParamKey != null ? "{" + pathParamKey + "}" : ""))
                .then().extract().response();
    }

    private void logAndAssert(String expectedMsg, String actualResponse, String logTitle, int expectedStatus) {
        Assert.assertTrue(actualResponse.contains(expectedMsg), "Expected message mismatch.");
        test.log(Status.INFO, "Response: " + actualResponse);
        test.log(Status.PASS, logTitle + " (Status: " + expectedStatus + ")");
    }

    private void prepareTest(String title) {
        test = report.createTest(title);
    }

    // ─────────────────────────────────────────────
    // ❌ Negative Tests
    // ─────────────────────────────────────────────

    @Test
    public void testCreateUserWithWeakPassword() {
        prepareTest("Create User with Weak Password");

        String body = """
            { "userName": "weakUser123", "password": "12345" }
            """;

        response = sendRequest("POST", CREATE_USER, body, false, null, null);
        Assert.assertEquals(response.statusCode(), 400);
        logAndAssert("Passwords must have", response.asString(), "Weak password rejected", 400);
    }

    @Test
    public void testGenerateTokenInvalidCredentials() {
        prepareTest("Generate Token with Invalid Credentials");

        String body = """
            { "userName": "nonexistentUser", "password": "wrongPass1!" }
            """;

        response = sendRequest("POST", GENERATE_TOKEN, body, false, null, null);
        Assert.assertEquals(response.statusCode(), 200);
        logAndAssert("Failed", response.jsonPath().getString("status"), "Invalid login attempt handled", 200);
    }

    @Test
    public void createBookInCollection() {
        prepareTest("Create Book without Authorization");

        String body = String.format("""
            { "userId": "%s", "collectionOfIsbns": [{ "isbn": "%s" }] }
            """, AccountsTest.id, ConfigManager.get("firstIsbn"));

        response = sendRequest("POST", ADD_BOOKS, body, false, null, null);
        Assert.assertEquals(response.statusCode(), 401);
        logAndAssert("User not authorized!", response.asString(), "Unauthorized book creation blocked", 401);
    }

    @Test
    public void testUserIsCreated() {
        prepareTest("Get User with Invalid ID");

        response = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", "invalidUserId")
                .get(GET_USER + "{userId}")
                .then()
                .statusCode(401)
                .extract().response();

        logAndAssert("User not authorized!", response.asString(), "Invalid user lookup blocked", 401);
    }

    @Test(dependsOnMethods = {"tests.AccountsTest.testCreateUser"})
    public void updateBookInCollection() {
        prepareTest("Update Book with Empty ISBN");

        String body = String.format("""
            { "userId": "%s", "isbn": "" }
            """, AccountsTest.id);

        response = sendRequest("PUT", UPDATE_BOOKS, body, true, "isbn", ConfigManager.get("firstIsbn"));
        Assert.assertEquals(response.statusCode(), 400);
        logAndAssert("Request Body is Invalid!", response.asString(), "Update without ISBN rejected", 400);
    }

    @Test
    public void deleteBookFromCollection() {
        prepareTest("Delete Book without User ID");

        String body = String.format("""
            { "userId": "", "isbn": "%s" }
            """, ConfigManager.get("firstIsbn"));

        response = sendRequest("DELETE", DELETE_BOOK, body, true, null, null);
        Assert.assertEquals(response.statusCode(), 401);
        logAndAssert("User Id not correct!", response.asString(), "Unauthorized delete blocked", 401);
    }
}
