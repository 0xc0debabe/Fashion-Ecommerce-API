package hmw.ecommerce.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderException extends RuntimeException{

    private final ErrorCode errorCode;
    private final List<Long> stockErrorList;

    public OrderException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.stockErrorList = null;
    }

    public OrderException(ErrorCode errorCode, List<Long> stockErrorList) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.stockErrorList = stockErrorList;
    }
}
