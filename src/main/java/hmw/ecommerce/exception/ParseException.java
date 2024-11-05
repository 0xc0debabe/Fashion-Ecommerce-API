package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class ParseException extends RuntimeException{

    private final ErrorCode errorCode;

    public ParseException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
