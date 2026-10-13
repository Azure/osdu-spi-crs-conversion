package org.opengroup.osdu.crs.middleware;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import jakarta.validation.ValidationException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private JaxRsDpsLog jaxRsDpsLog;

    @ExceptionHandler(value = { BadRequestException.class})
    public ResponseEntity<AppError> handleBadRequest(Exception  e) {
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Bad request")
                .reason("Error")
                .build();
        jaxRsDpsLog.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(appError);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class })
    public ResponseEntity<AppError> handleIllegalRequest(Exception  e) {
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Bad request")
                .reason(e.getMessage())
                .build();
        jaxRsDpsLog.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(appError);
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<AppError> handleValidationException(ValidationException e) {
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
        jaxRsDpsLog.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(appError);
    }

    @ExceptionHandler(value = HttpMessageConversionException.class)
    public ResponseEntity<AppError> handleHttpMessageConversionException(HttpMessageConversionException e){
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
        jaxRsDpsLog.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(appError);
    }

    @NonNull
    @Override
    public ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex, @NonNull HttpHeaders headers,
                                                                @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        AppError appError = AppError.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("Resource not found.")
                .reason("Resource not found.")
                .build();
        jaxRsDpsLog.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(appError);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        return getBadInputResponse(ex);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        return getBadInputResponse(ex);
    }

    @ExceptionHandler(value = HttpStatusCodeException.class)
    public ResponseEntity<AppError> handleHttpStatusCodeException(HttpStatusCodeException e) {
        HttpStatusCode statusCode= e.getStatusCode();
        if (statusCode == HttpStatus.METHOD_NOT_ALLOWED) {
            return getResponse(new AppException(statusCode.value(), "Method not allowed.", "Method not allowed.", e));
        }
        return getResponse(new AppException(statusCode.value(), "Server error.", "An unknown error has occurred.", e));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<AppError> handleGenericException(Exception e) {
        if (e instanceof AppException) {
            return getResponse((AppException) e);
        } else if (e instanceof StringIndexOutOfBoundsException) {
            /*When invalid input types are passed(UnitXY or UnitZ fields with values like '^$#@><!()',), it causes StringIndexOutOfBoundsException.
                GetBadInputResponse below method does not handle these cases.
                Handled it with generic error with code 400 (Bad Request).*/
            return getResponse(new AppException(HttpStatus.BAD_REQUEST.value(), "Bad input type or format.", "Please check the input type and format and try again."));
        } else {
            return getResponse(new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error.", "An unknown error has occurred.", e));
        }
    }

    private ResponseEntity<Object> getBadInputResponse(Exception e) {
        String errorMessage = "bad input type or format, check the input type and format.";
        AppError appError = AppError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .build();
        jaxRsDpsLog.error(e.getMessage(), e);
        return ResponseEntity.badRequest().body(appError);
    }


    private ResponseEntity<AppError> getResponse(AppException appException) {
        if (appException.getCause() instanceof Exception) {
            Exception original = (Exception) appException.getCause();
            jaxRsDpsLog.error(original.getMessage(), original);
        }
        jaxRsDpsLog.error(appException.getError().getMessage(), appException);
        AppError appError = appException.getError();
        return ResponseEntity.status(appError.getCode()).contentType(MediaType.APPLICATION_JSON).body(appError);
    }
}
