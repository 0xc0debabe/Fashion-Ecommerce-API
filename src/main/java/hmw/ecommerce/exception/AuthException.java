package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{

    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
