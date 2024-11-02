package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class EmailException extends RuntimeException{

    private final ErrorCode errorCode;

    public EmailException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
