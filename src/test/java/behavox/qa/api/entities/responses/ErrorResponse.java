package behavox.qa.api.entities.responses;


public class ErrorResponse {
    public String timestamp, error, message, path;
    public int status;

    public ErrorResponse() {

    }

    public ErrorResponse(String path, String error, String message, int status) {
        this.path = path;
        this.error = error;
        this.message = message;
        this.status = status;
    }
}
