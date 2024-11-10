package hmw.ecommerce.exception;

import lombok.Getter;

@Getter
public class ItemException extends RuntimeException{

    private final ErrorCode errorCode;

    public ItemException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
