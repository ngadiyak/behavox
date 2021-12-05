package behavox.qa.helpers;

import io.restassured.filter.Filter;
import io.restassured.response.ValidatableResponse;

import java.util.Map;

import static behavox.qa.Constants.STATUS_PATH;
import static behavox.qa.Constants.SUBMIT_PATH;
import static io.restassured.RestAssured.given;


public class ApiHelper {

    public ValidatableResponse submitNoFilters(Map<String, String> body) {
        return given()
                .body(body)
                .noFilters()
                .when()
                .post(SUBMIT_PATH)
                .then();
    }

    public ValidatableResponse submit(Map<String, String> body, Filter filter) {
        return given()
                .body(body)
                .filter(filter)
                .when()
                .post(SUBMIT_PATH)
                .then();
    }

    public ValidatableResponse submit(Map<String, String> body) {
        return given()
                .body(body)
                .when()
                .post(SUBMIT_PATH)
                .then();
    }

    public <T> T submit (Class<T> clazz, Map<String, String> body, int expectedStatusCode) {
        return given()
                .body(body)
                .when()
                .post(SUBMIT_PATH)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .response()
                .andReturn()
                .as(clazz);
    }

    public ValidatableResponse statusNoFilters(String id) {
        return given()
                .noFilters()
                .get(STATUS_PATH, id)
                .then();
    }

    public ValidatableResponse status(String id) {
        return given()
                .when()
                .get(STATUS_PATH, id)
                .then();
    }

    public ValidatableResponse status(String id, Filter filter) {
        return given()
                .filter(filter)
                .when()
                .get(STATUS_PATH, id)
                .then();
    }

    public <T> T status (Class<T> clazz, String id, int expectedStatusCode) {
        return given()
                .when()
                .get(STATUS_PATH, id)
                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .response()
                .andReturn()
                .as(clazz);
    }
}
