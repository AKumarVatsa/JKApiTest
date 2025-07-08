package steps;

import static constants.EndPoints.*;
import tests.AccountsTest;
import base.BaseTest;
import config.ConfigManager;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

public class BookSteps extends BaseTest {

    public static String isbnNumber;
    public static String bookTitle;
    public Response response;

    private String getAuthHeader() {
        return "Bearer " + AccountsTest.token;
    }

    private String createBookRequestBody(String userId, String isbn) {
        return String.format("""
            {
              "userId": "%s",
              "collectionOfIsbns": [{ "isbn": "%s" }]
            }
            """, userId, isbn);
    }

    private String createUpdateDeleteBody(String userId, String isbn) {
        return String.format("""
            {
              "userId": "%s",
              "isbn": "%s"
            }
            """, userId, isbn);
    }

    @Given("the user is authenticated")
    public void userIsAuthenticated() {
        assertNotNull(AccountsTest.token);
        assertNotNull(AccountsTest.id);
    }

    @When("the user adds a book to the collection")
    public void addBook() {
        String body = createBookRequestBody(AccountsTest.id, ConfigManager.get("firstIsbn"));
        response = given()
                .header("Authorization", getAuthHeader())
                .contentType("application/json")
                .body(body)
                .when()
                .post(ADD_BOOKS);

        response.then().statusCode(201);
        isbnNumber = response.jsonPath().getString("books[0].isbn");
    }

    @Then("the book should be added successfully")
    public void verifyBookAdded() {
        assertNotNull(isbnNumber);
    }

    @When("the user retrieves all books")
    public void getAllBooks() {
        response = given().when().get(GET_BOOKS);
        response.then().statusCode(200);
        bookTitle = response.jsonPath().getString("books[0].title");
    }

    @Then("the book list should contain the expected book title")
    public void verifyBookTitle() {
        assertNotNull(bookTitle);
    }

    @When("the user attempts to update a book")
    public void updateBook() {
        String body = createUpdateDeleteBody(AccountsTest.id, isbnNumber);
        response = given()
                .header("Authorization", getAuthHeader())
                .contentType("application/json")
                .body(body)
                .pathParam("isbn", isbnNumber)
                .when()
                .put(UPDATE_BOOKS + "{isbn}");
    }

    @Then("the update should not be supported")
    public void verifyUpdateUnsupported() {
        assertEquals(response.statusCode(), 400);
    }

    @When("the user deletes the book")
    public void deleteBook() {
        String body = createUpdateDeleteBody(AccountsTest.id, isbnNumber);
        response = given()
                .header("Authorization", getAuthHeader())
                .contentType("application/json")
                .body(body)
                .when()
                .delete(DELETE_BOOK);
    }

    @Then("the book should be removed successfully")
    public void verifyBookDeleted() {
        assertEquals(response.statusCode(), 204);
    }

    @When("the user checks the collection")
    public void checkIfDeleted() {
        response = given()
                .header("Authorization", getAuthHeader())
                .when()
                .get(GET_USER + AccountsTest.id);
    }

    @Then("the book should not be present")
    public void verifyDeletedFromCollection() {
        String responseBody = response.getBody().asString();
        assertFalse(responseBody.contains(isbnNumber));
    }
}
