package hmw.ecommerce.exception.exceptions;

import hmw.ecommerce.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ParseException extends RuntimeException{

    private final ErrorCode errorCode;

    public ParseException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
