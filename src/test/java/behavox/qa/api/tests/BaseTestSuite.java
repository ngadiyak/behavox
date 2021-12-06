package behavox.qa.api.tests;

import behavox.qa.api.entities.responses.ErrorResponse;
import behavox.qa.api.entities.responses.ExecutionStatuses;
import behavox.qa.api.entities.responses.StatusResponse;
import behavox.qa.filters.BasicAuthFilter;
import behavox.qa.helpers.ApiHelper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.val;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static behavox.qa.Constants.*;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;


@Testcontainers
public class BaseTestSuite {

    @Container
    protected static final GenericContainer service = new GenericContainer(DockerImageName.parse(System.getenv().getOrDefault("TEST_IMAGE", TEST_IMAGE)))
            .withExposedPorts(BASE_API_PORT)
            .waitingFor(Wait.forLogMessage(".*Started GroovyApplicationKt.*", 1));

    protected ApiHelper api = new ApiHelper();

    protected static final RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_API_URI)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .build();

    @BeforeAll
    public static void beforeAll() {
        RestAssured.requestSpecification = requestSpec
                .filters(new BasicAuthFilter())
                .port(service.getFirstMappedPort())
                .log().ifValidationFails(LogDetail.ALL);
    }

    protected Map<String, String> mkRandomSubmitBody() {
        return mkSubmitBody(String.valueOf(System.currentTimeMillis()));
    }

    protected Map<String, String> mkSubmitBody(String code) {
        return new HashMap<String, String>() {{
            put("code", code);
        }};
    }

    protected void validateError(ErrorResponse expected, ErrorResponse actual) {
        assertThat(LocalDateTime.now(ZoneOffset.UTC), LocalDateTimeMatchers.within(2, ChronoUnit.SECONDS, LocalDateTime.parse(actual.timestamp.substring(0, 23))));
        Assertions.assertEquals(expected.status, actual.status);
        Assertions.assertEquals(expected.error, actual.error);
        Assertions.assertEquals(expected.message, actual.message);
        Assertions.assertEquals(expected.path, actual.path);
    }

    protected void validateHttpStatusCode(ValidatableResponse response, int expectedStatusCode) {
        response.statusCode(expectedStatusCode);
    }

    protected void validateStatusEventually(String expectedId, String expectedResult, ExecutionStatuses expectedStatus) {

        Object obj = new Object();
        try {
            synchronized (obj) {
                for (int i = 0; i < STATUS_TIMEOUT_SEC; i++) {
                    if (!api.status(StatusResponse.class, expectedId, HTTP_OK).status.equals(expectedStatus.name()))
                        obj.wait(1000L);
                }
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

        val response = api.status(StatusResponse.class, expectedId, HTTP_OK);

        Assertions.assertEquals(expectedId, response.id);
        Assertions.assertEquals(expectedResult, response.result);
    }

    protected static Stream<Arguments> authIncorrectParameters() {
        return Stream.of(
                Arguments.of("user_1", "pass_2"),
                Arguments.of("user_2", ""),
                Arguments.of("", "pass_1"),
                Arguments.of("", ""),
                Arguments.of("user_5", "pass_5111"),
                Arguments.of("user_6", "pass_6")
        );
    }

}
