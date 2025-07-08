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
    public static String id;

    public Response response;

    // ─────────────────────────────────────────────
    // 🔧 Utility Methods
    // ─────────────────────────────────────────────

    private String createRequestBody(String username, String password) {
        return String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, password);
    }

    private Response sendRequest(String method, String endpoint, String body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .request(method, endpoint)
                .then()
                .extract()
                .response();
    }

    private void logAndAssertResponse(String title, String responseBody, boolean condition, String passMessage) {
        test.log(Status.INFO, title + ": " + responseBody);
        System.out.println(title + ": " + responseBody);
        Assert.assertTrue(condition, "Assertion failed for: " + title);
        test.log(Status.PASS, passMessage);
    }

    // ─────────────────────────────────────────────
    // ✅ Test Methods
    // ─────────────────────────────────────────────

    @Test(priority = 1)
    public void testCreateUser() {
        test = report.createTest("Create User Test");

        String requestBody = createRequestBody(username, ConfigManager.get("password"));

        response = sendRequest("POST", CREATE_USER, requestBody);
        Assert.assertEquals(response.statusCode(), 201, "Create User - Unexpected Status Code");

        id = response.jsonPath().getString("userID");
        logAndAssertResponse("Create User Response", response.getBody().asString(), id != null, "User created with ID: " + id);
    }

    @Test(priority = 2, dependsOnMethods = "testCreateUser")
    public void testGenerateToken() {
        test = report.createTest("Generate Token Test");

        String requestBody = createRequestBody(username, ConfigManager.get("password"));

        response = sendRequest("POST", GENERATE_TOKEN, requestBody);
        Assert.assertEquals(response.statusCode(), 200, "Generate Token - Unexpected Status Code");

        token = response.jsonPath().getString("token");
        logAndAssertResponse("Generate Token Response", response.getBody().asString(), token != null, "Token generated: " + token);
    }

    @Test(priority = 3, dependsOnMethods = "testCreateUser")
    public void testUserIsCreated() {
        test = report.createTest("Get Created User Details");

        response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("userId", id)
                .when()
                .get(GET_USER + "{userId}")
                .then()
                .statusCode(200)
                .extract()
                .response();

        logAndAssertResponse("Get User Response", response.getBody().asString(), response.statusCode() == 200, "User details fetched successfully.");
    }
}
