package br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in;

import br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in.dto.ErrorDTO;
import br.gabrielsmartins.smartpayment.frauds.adapters.web.adapter.in.dto.ErrorDTO.ErrorFieldDTO;
import br.gabrielsmartins.smartpayment.frauds.application.exception.FraudNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    @ExceptionHandler(FraudNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleFraudNotFoundException(FraudNotFoundException ex){
        log.warn("Invalid Request", ex);
        return ErrorDTO.builder()
                .withCode(404)
                .withMessage(ex.getMessage())
                .build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleMethodArgumentNotValidException(WebExchangeBindException ex) {
        log.warn("Invalid Request", ex);
        ErrorDTO errorDTO = ErrorDTO.builder()
                .withCode(400)
                .withMessage(ex.getMessage())
                .build();
        ex.getFieldErrors().forEach(fieldError -> {
            var errorFieldDTO = ErrorFieldDTO.builder()
                                                                 .withName(fieldError.getField())
                                                                 .withValue(fieldError.getRejectedValue())
                                                                 .withMessage(fieldError.getDefaultMessage())
                                                                 .build();
            errorDTO.addField(errorFieldDTO);
        });
        return errorDTO;
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleException(Exception ex) {
        log.error("Error processing request",ex);
        return ErrorDTO.builder()
                        .withCode(500)
                        .withMessage(ex.getMessage())
                        .build();
    }
}
