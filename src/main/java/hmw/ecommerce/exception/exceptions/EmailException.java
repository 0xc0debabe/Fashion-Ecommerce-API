package hmw.ecommerce.exception.exceptions;

import hmw.ecommerce.exception.ErrorCode;
import lombok.Getter;

@Getter
public class EmailException extends RuntimeException{

    private final ErrorCode errorCode;

    public EmailException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
