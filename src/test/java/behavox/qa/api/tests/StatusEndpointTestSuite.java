package behavox.qa.api.tests;

import behavox.qa.api.entities.responses.ErrorResponse;
import behavox.qa.api.entities.responses.SubmitResponse;
import behavox.qa.filters.BasicAuthFilter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static behavox.qa.Constants.STATUS_BASE_PATH;
import static behavox.qa.api.entities.responses.ExecutionStatuses.*;
import static java.net.HttpURLConnection.*;

@Tag("Status")
public class StatusEndpointTestSuite extends BaseTestSuite {

    @Test
    public void shouldReturnCompletedStatusAfterExecution() {
        val id = api.submit(SubmitResponse.class, mkSubmitBody("10 + 10"), HTTP_OK).id;

        validateStatusEventually(id, "20", COMPLETED);
    }

    @Test
    public void shouldReturnEmptyResultWhileRequestIsInProgress() {
        val id = api.submit(SubmitResponse.class, mkSubmitBody("while(true) {};"), HTTP_OK).id;

        validateStatusEventually(id, null, IN_PROGRESS);
    }

    @Test
    public void shouldReturnFailedStatusWhenCodeIsIncorrect() {
        val error = "java.util.concurrent.ExecutionException: groovy.lang.MissingMethodException: " +
                "No signature of method: Script1.val() is applicable for argument types: (Integer)" +
                " values: [1]\nPossible solutions: tap(groovy.lang.Closure), wait(), run(), run()" +
                ", any(), wait(long)";
        val id = api.submit(SubmitResponse.class, mkSubmitBody("val a = 1; a += 1"), HTTP_OK).id;

        validateStatusEventually(id, error, FAILED);
    }

    @Test
    public void shouldReturnErrorForIncorrectId() {
        val errorResponse = api.status("incorrect")
                .statusCode(HTTP_BAD_REQUEST)
                .extract()
                .response()
                .andReturn()
                .as(ErrorResponse.class);

        validateError(new ErrorResponse(STATUS_BASE_PATH, "Bad Request", "", HTTP_BAD_REQUEST), errorResponse);
    }

    @Test
    public void shouldReturn401WithoutAuth() {
        validateHttpStatusCode(api.statusNoFilters(UUID.randomUUID().toString()), HTTP_UNAUTHORIZED);
    }

    @ParameterizedTest(name = "shouldReturn401WithoutCorrectBasicAuth | [user: {0}; pass:{1}]")
    @MethodSource("authIncorrectParameters")
    public void shouldReturn401WithoutCorrectBasicAuth(String user, String password) {
        validateHttpStatusCode(api.status(UUID.randomUUID().toString(), new BasicAuthFilter(user, password)), HTTP_UNAUTHORIZED);
    }

    @Test
    public void shouldGiveResultsOnlyForRequestOwner() {
        val submitResponse = api.submit(SubmitResponse.class, mkRandomSubmitBody(), HTTP_OK);
        val id = submitResponse.id;

        validateHttpStatusCode(api.status(id, new BasicAuthFilter("user_2", "pass_2")), HTTP_FORBIDDEN);
    }

    @Test
    public void shouldReturnErrorForNotExistedId() {
        val errorResponse = api.status(UUID.randomUUID().toString())
                .statusCode(HTTP_NOT_FOUND)
                .extract()
                .response()
                .andReturn()
                .as(ErrorResponse.class);

        validateError(new ErrorResponse(STATUS_BASE_PATH, "Not Found", "", HTTP_NOT_FOUND), errorResponse);
    }
}
