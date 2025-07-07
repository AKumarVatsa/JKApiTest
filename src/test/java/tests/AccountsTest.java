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

    private static final String PASSWORD = ConfigManager.get("password");
    private static final String AUTH_HEADER = "Authorization";

    private static final String username = "user" + (int) (Math.random() * 10_000);
    private static String token;
    private static String userId;

    private Response sendPostRequest(String endpoint, String body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    private String buildAuthBody() {
        return String.format("""
                {
                  "userName": "%s",
                  "password": "%s"
                }
                """, username, PASSWORD);
    }

    @Test(priority = 1)
    public void testCreateUser() {
        test = report.createTest("Create User Test");

        Response respon
