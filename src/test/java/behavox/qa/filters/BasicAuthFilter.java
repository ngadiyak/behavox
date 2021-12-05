package behavox.qa.filters;

import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.spi.AuthFilter;

import static behavox.qa.Constants.*;

public class BasicAuthFilter implements AuthFilter {

    private String user = CORRECT_USER;
    private String password = CORRECT_PASSWORD;

    public BasicAuthFilter() {
    }

    public BasicAuthFilter(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        requestSpec.auth().basic(user, password);
        return ctx.next(requestSpec, responseSpec);
    }
}
