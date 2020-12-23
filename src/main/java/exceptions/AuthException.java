package exceptions;

public class AuthException extends SetuException{
    public AuthException(String message, String requestId, String code, Integer statusCode) {
        super(message, requestId, code, statusCode);
    }
}
