package ee.kjlellep.energyaggregator.common.web;

import ee.kjlellep.energyaggregator.common.web.dto.ErrorResponse;
import ee.kjlellep.energyaggregator.prices.web.PriceController;
import ee.kjlellep.energyaggregator.query.web.AggregatorQueryController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = {
    PriceController.class,
    AggregatorQueryController.class
})
public class RestExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
