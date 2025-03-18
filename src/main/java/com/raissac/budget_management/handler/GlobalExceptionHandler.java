package com.raissac.budget_management.handler;

import com.raissac.budget_management.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException ex) {

        logger.error("Validation exception occurred", ex);

        Set<String> errors = new HashSet<>();

        ex.getBindingResult().getAllErrors().forEach(
                error -> errors.add(error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.BAD_REQUEST.value())
                        .validationErrors(errors)
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException ex) {

        logger.error("Bad credentials", ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.UNAUTHORIZED.value())
                        .error("Invalid credentials")
                        .errorDescription("Email or password is inncorrect")
                        .build());
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ExceptionResponse> handleException(EmailAlreadyUsedException ex) {

        logger.error("Email already used!", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.CONFLICT.value())
                        .errorDescription("Email already used!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleException(CategoryAlreadyExistsException ex) {

        logger.error("Category already exists!", ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.CONFLICT.value())
                        .errorDescription("Category already exists!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(CategoryNotFoundException ex) {

        logger.error("Category not found!", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.NOT_FOUND.value())
                        .errorDescription("Category not found!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserNotFoundException ex){

        logger.error("User not found!", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.NOT_FOUND.value())
                        .errorDescription("User not found!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccessDeniedException ex){

        logger.error("Access denied!", ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.UNAUTHORIZED.value())
                        .errorDescription("Access denied!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(TransactionNotFoundException ex){

        logger.error("Transaction not found!", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.NOT_FOUND.value())
                        .errorDescription("Transaction not found!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(CsvExportException.class)
    public ResponseEntity<ExceptionResponse> handleException(CsvExportException ex){

        logger.error("CSV export error!", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorDescription("CSV export error!")
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {

        logger.error("Internal error, please contact the admin", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionResponse.builder()
                        .errorDescription("Internal error, please contact the admin")
                        .error(ex.getMessage())
                        .build());
    }
}
