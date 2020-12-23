package exceptions;

public abstract class SetuException extends Exception {
    private String code;
    private String requestId;
    private Integer statusCode;

    protected SetuException(String message, String requestId, String code, Integer statusCode) {
        this(message, requestId, code, statusCode, null);
    }

    protected SetuException(String message, String requestId, String code, Integer statusCode, Throwable e) {
        super(message, e);
        this.code = code;
        this.requestId = requestId;
        this.statusCode = statusCode;
    }

    /**
     * Returns a String representation of the error.
     *
     * @return Error string
     * */
    @Override
    public String getMessage() {
        String additionalInfo = "";
        if (code != null) {
            additionalInfo += ";code :" + code;
        }
        return super.getMessage();
    }
}
