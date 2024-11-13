package hmw.ecommerce.exception.exceptions;

import hmw.ecommerce.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{

    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
