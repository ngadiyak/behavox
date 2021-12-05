package behavox.qa.api.tests;

import behavox.qa.api.entities.responses.ErrorResponse;
import behavox.qa.api.entities.responses.SubmitResponse;
import behavox.qa.filters.BasicAuthFilter;
import lombok.val;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.stream.Stream;

import static behavox.qa.Constants.SUBMIT_PATH;
import static behavox.qa.api.entities.responses.ExecutionStatuses.*;
import static java.net.HttpURLConnection.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;


public class SubmitEndpointTestSuite extends BaseTestSuite {

    @ParameterizedTest
    @MethodSource("submitCorrectParameters")
    public void shouldExecuteCodeAndReturnCompletedResult(String code, String result) {
        val id = api.submit(SubmitResponse.class, mkSubmitBody(code), HTTP_OK).id;

        assertThat(id.length(), greaterThan(0));
        validateStatusEventually(id, result, COMPLETED);
    }

    @Test
    public void shouldCorrectlyHandleIncorrectGroovyCode() {
        val id = api.submit(SubmitResponse.class, mkSubmitBody("val a = 1; a += 1"), HTTP_OK).id;

        assertThat(id.length(), greaterThan(0));
    }

    @Test
    public void shouldHandleSameRequestsTwice() {
        validateHttpStatusCode(api.submit(mkSubmitBody("1 + 100")), HTTP_OK);
        validateHttpStatusCode(api.submit(mkSubmitBody("1 + 100")), HTTP_OK);
    }

    @Test
    public void shouldExecuteOnlyTwoRequestsInOneMoment() {
        val requestId0 = api.submit(SubmitResponse.class, mkSubmitBody("10 + 1"), HTTP_OK).id;
        val requestId1 = api.submit(SubmitResponse.class, mkSubmitBody("sleep(15000); 1 + 1"), HTTP_OK).id;
        val requestId2 = api.submit(SubmitResponse.class, mkSubmitBody("sleep(5000); 1 + 2"), HTTP_OK).id;

        validateStatusEventually(requestId0, "11", COMPLETED);
        validateStatusEventually(requestId1, null, IN_PROGRESS);
        validateStatusEventually(requestId2, null, IN_PROGRESS);

        val requestId3 = api.submit(SubmitResponse.class, mkSubmitBody("1 + 1"), HTTP_OK).id;

        validateStatusEventually(requestId3, null, PENDING);
        validateStatusEventually(requestId2, "3", COMPLETED);
        validateStatusEventually(requestId3, "2", COMPLETED);
    }

    @Test
    public void shouldReturnErrorWhenBodyIsIncorrect() {
        val errorResponse = api.submit(new HashMap<>())
                .statusCode(HTTP_BAD_REQUEST)
                .extract()
                .response()
                .andReturn()
                .as(ErrorResponse.class);

        assertThat(LocalDateTime.now(ZoneOffset.UTC), LocalDateTimeMatchers.within(2, ChronoUnit.SECONDS, LocalDateTime.parse(errorResponse.timestamp.substring(0, 23))));
        Assertions.assertEquals(HTTP_BAD_REQUEST, errorResponse.status);
        Assertions.assertEquals("Bad Request", errorResponse.error);
        Assertions.assertEquals("", errorResponse.message);
        Assertions.assertEquals(SUBMIT_PATH, errorResponse.path);
    }

    @Test
    public void shouldReturn401WithoutAuth() {
        validateHttpStatusCode(api.submitNoFilters(mkRandomSubmitBody()), HTTP_UNAUTHORIZED);
    }

    @ParameterizedTest
    @MethodSource("authIncorrectParameters")
    public void shouldReturn401WithoutCorrectBasicAuth(String user, String password) {
        validateHttpStatusCode(api.submit(mkRandomSubmitBody(), new BasicAuthFilter(user, password)), HTTP_UNAUTHORIZED);
    }

    private static Stream<Arguments> submitCorrectParameters() {
        return Stream.of(
                Arguments.of("1 + 50", "51"),
                Arguments.of("def a = -10; a", "-10"),
                Arguments.of("def a = 2; if(a==2) 13 else -1", "13"),
                Arguments.of("def multiple(a, b) { return a * b }; def a = 2; def b = 4; multiple(2, 4)", "8"),
                Arguments.of("def birdArr = [\"Parrot\", \"Cockatiel\", \"Pigeon\"]; birdArr", "[Parrot, Cockatiel, Pigeon]")
        );
    }
}
