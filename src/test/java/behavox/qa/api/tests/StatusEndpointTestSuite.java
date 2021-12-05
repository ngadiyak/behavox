package behavox.qa.api.tests;

import behavox.qa.api.entities.responses.ErrorResponse;
import behavox.qa.api.entities.responses.SubmitResponse;
import behavox.qa.filters.BasicAuthFilter;
import lombok.val;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static behavox.qa.Constants.STATUS_BASE_PATH;
import static behavox.qa.api.entities.responses.ExecutionStatuses.*;
import static java.net.HttpURLConnection.*;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void shouldReturn401WithoutAuth() {
        validateHttpStatusCode(api.statusNoFilters(UUID.randomUUID().toString()), HTTP_UNAUTHORIZED);
    }

    @ParameterizedTest
    @MethodSource("authIncorrectParameters")
    public void shouldReturn401WithoutCorrectBasicAuth(String user, String password) {
        validateHttpStatusCode(api.status(UUID.randomUUID().toString(), new BasicAuthFilter(user, password)), HTTP_UNAUTHORIZED);
    }


    //BUG
    @Test
    public void shouldGiveResultsOnlyForRequestOwner() {
        val submitResponse = api.submit(SubmitResponse.class, mkRandomSubmitBody(), HTTP_OK);
        val id = submitResponse.id;

        validateHttpStatusCode(api.status(id, new BasicAuthFilter("user_2", "pass_2")), HTTP_FORBIDDEN);
    }

    @Test
    public void shouldReturnErrorForIncorrectId() {
        val errorResponse = api.status("incorrect")
                .statusCode(HTTP_BAD_REQUEST)
                .extract()
                .response()
                .andReturn()
                .as(ErrorResponse.class);

        assertThat(LocalDateTime.now(ZoneOffset.UTC), LocalDateTimeMatchers.within(2, ChronoUnit.SECONDS, LocalDateTime.parse(errorResponse.timestamp.substring(0, 23))));
        Assertions.assertEquals(HTTP_BAD_REQUEST, errorResponse.status);
        Assertions.assertEquals("Bad Request", errorResponse.error);
        Assertions.assertEquals("", errorResponse.message);
        Assertions.assertEquals(STATUS_BASE_PATH, errorResponse.path);
    }

    @Test
    public void shouldReturnErrorForNotExistedId() {
        val errorResponse = api.status(UUID.randomUUID().toString())
                .statusCode(HTTP_NOT_FOUND)
                .extract()
                .response()
                .andReturn()
                .as(ErrorResponse.class);

        assertThat(LocalDateTime.now(ZoneOffset.UTC), LocalDateTimeMatchers.within(2, ChronoUnit.SECONDS, LocalDateTime.parse(errorResponse.timestamp.substring(0, 23))));
        Assertions.assertEquals(HTTP_NOT_FOUND, errorResponse.status);
        Assertions.assertEquals("Not Found", errorResponse.error);
        Assertions.assertEquals("", errorResponse.message);
        Assertions.assertEquals(STATUS_BASE_PATH, errorResponse.path);
    }
}
