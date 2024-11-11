package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class CartException extends RuntimeException{

    private final ErrorCode errorCode;

    public CartException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
