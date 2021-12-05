package behavox.qa;

public class Constants {

    public static final String TEST_IMAGE = "behavox/product-qa-groovy-1.0.1:latest";

    public static final int STATUS_TIMEOUT_SEC = 10;

    //region AUTH

    public static final String CORRECT_USER = "user_1";
    public static final String CORRECT_PASSWORD = "pass_1";

    //endregion

    //region API

    public static final String BASE_API_URI = "http://localhost";
    public static final int BASE_API_PORT= 8080;

    public static final String SUBMIT_PATH = "/groovy/submit";
    public static final String STATUS_BASE_PATH = "/groovy/status";
    public static final String STATUS_PATH = "/groovy/status?id={id}";

    //endregion
}
