package com.chiacademy.software.phonecontacts.exception.handler;

import com.chiacademy.software.phonecontacts.exception.Error;
import com.chiacademy.software.phonecontacts.exception.NotFoundException;
import com.chiacademy.software.phonecontacts.exception.UserAlreadyExistsException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private final static String PROBLEMS = "problemDetails";
    public static final String MUST_HAVE_A_VALID_TYPE = "The field '%s' must have a valid type of '%s'";


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "User login already exists");
        Error error = Error.builder().message(e.getMessage()).wrongValue(e.getWrongValue()).build();
        pd.setProperty(PROBLEMS, List.of(error));
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Data is not found");
        Error error = Error.builder().message(e.getMessage()).wrongValue(e.getWrongValue()).build();
        pd.setProperty(PROBLEMS, List.of(error));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed validation");
        List<Error> errors = new ArrayList<>();
        List<FieldError> fieldErrorList = e.getBindingResult().getFieldErrors();
        for (FieldError err : fieldErrorList) {
            Error error = Error.builder()
                    .message(err.getDefaultMessage())
                    .field(err.getField())
                    .wrongValue(err.getRejectedValue() == null ? null : err.getRejectedValue().toString()).build();
            errors.add(error);
        }
        pd.setProperty(PROBLEMS, errors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Values can not be read");
        Throwable throwable = e.getCause();
        Error error = Error.builder().message(e.getMessage()).build();
        if (throwable instanceof InvalidTypeIdException exception) {
            String wrongValue = exception.getTypeId();
            error = Error.builder().message(e.getMessage()).wrongValue(wrongValue).build();
        } else if (throwable instanceof InvalidFormatException exception) {
            String wrongValue = exception.getValue().toString();
            error = Error.builder().message(e.getMessage()).wrongValue(wrongValue).build();
        }
        pd.setProperty(PROBLEMS, List.of(error));
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Wrong input parameter");
        String actualField = e.getName();
        Object wrongValue = Optional.ofNullable(e.getValue()).orElse("");
        String requiredType = "";
        if (e.getRequiredType() !=  null) {
            requiredType = e.getRequiredType().getSimpleName();
        }
        String message = String.format(MUST_HAVE_A_VALID_TYPE, actualField, requiredType);
        Error error = Error.builder().message(message).field(actualField).wrongValue(wrongValue.toString()).build();
        pd.setProperty(PROBLEMS, List.of(error));
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Constraint violation");
        Set<ConstraintViolation<?>> cvSet = e.getConstraintViolations();
        List<Error> errors = new ArrayList<>();
        for (var cv : cvSet) {
            String field = cv.getPropertyPath().toString().substring(cv.getPropertyPath().toString().lastIndexOf('.') + 1);
            String value = cv.getInvalidValue().toString();
            String message = String.format(cv.getMessage(), field);
            Error error = Error.builder().message(message).field(field).wrongValue(value).build();
            errors.add(error);
        }
        problemDetail.setProperty(PROBLEMS, errors);
        return problemDetail;
    }
}
