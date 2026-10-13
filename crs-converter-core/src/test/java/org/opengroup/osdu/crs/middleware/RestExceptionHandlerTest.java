package org.opengroup.osdu.crs.middleware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ValidationException;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class RestExceptionHandlerTest {

    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @InjectMocks
    private RestExceptionHandler restExceptionHandler;

    @Test
    public void shouldReturnBadRequest() {
        BadRequestException exception = new BadRequestException("Bad request");

        ResponseEntity<AppError> response = restExceptionHandler.handleBadRequest(exception);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("Error", body.getReason());
        assertEquals("Bad request", body.getMessage());
        Mockito.verify(jaxRsDpsLog).error("400 Bad request", exception);
    }

    @Test
    public void shouldReturnBadRequestOnValidationException() {
        ValidationException exception = new ValidationException("Bad request");

        ResponseEntity<AppError> response = restExceptionHandler.handleValidationException(exception);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("Bad request", body.getMessage());
        Mockito.verify(jaxRsDpsLog).error("Bad request", exception);
    }

    @Test
    public void shouldReturnBadRequestOnHttpMessageConversionException() {
        HttpMessageConversionException exception = new HttpMessageConversionException("Bad request");

        ResponseEntity<AppError> response = restExceptionHandler.handleHttpMessageConversionException(exception);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("Bad request", body.getMessage());
        Mockito.verify(jaxRsDpsLog).error("Bad request", exception);
    }

    @Test
    public void shouldUseValuesInAppExceptionInResponseOnAppException() {
        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<AppError> response = restExceptionHandler.handleGenericException(exception);

        assertEquals(409, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(409, body.getCode());
        assertEquals("any reason", body.getReason());
        assertEquals("any message", body.getMessage());
        jaxRsDpsLog.error(exception.getError().getMessage(), exception);
    }

    @Test
    public void shouldUseValuesInResponseOnStringIndexOutOfBoundsException() {
        StringIndexOutOfBoundsException stException = new StringIndexOutOfBoundsException("Index out of Bound");

        ResponseEntity<AppError> response = restExceptionHandler.handleGenericException(stException);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("Bad input type or format.", body.getReason());
        assertEquals("Please check the input type and format and try again.", body.getMessage());
        jaxRsDpsLog.error(stException.getMessage(), stException);
    }

    @Test
    public void shouldUseGenericValuesInResponseOnException() {
        Exception exception = new Exception("any message");

        ResponseEntity<AppError> response = restExceptionHandler.handleGenericException(exception);

        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(500, body.getCode());
        assertEquals("Server error.", body.getReason());
        assertEquals("An unknown error has occurred.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }

    @Test
    public void shouldReturnNotFoundResponse() {
        NoHandlerFoundException exception = new NoHandlerFoundException("Any method", "Any requestURL", new HttpHeaders());

        ResponseEntity<Object> response = restExceptionHandler.handleNoHandlerFoundException(exception, new HttpHeaders(), HttpStatusCode.valueOf(404), mock(WebRequest.class));

        assertEquals(404, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = (AppError) response.getBody();
        assertEquals(404, body.getCode());
        assertEquals("Resource not found.", body.getReason());
        assertEquals("Resource not found.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }

    @Test
    public void shouldReturnBadRequestOnMethodNotAllowedException() throws Exception {
        MethodParameter methodParameter = mock(MethodParameter.class);
        Method executable = Object.class.getMethod("toString");
        Mockito.when(methodParameter.getExecutable()).thenReturn(executable);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, mock(BindingResult.class));

        ResponseEntity<Object> response = restExceptionHandler
                .handleMethodArgumentNotValid(exception, new HttpHeaders(), HttpStatusCode.valueOf(404), mock(WebRequest.class));

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = (AppError) response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("bad input type or format, check the input type and format.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }

    @Test
    public void shouldReturnBadRequestOnHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("error");

        ResponseEntity<Object> response = restExceptionHandler
                .handleHttpMessageNotReadable(exception, new HttpHeaders(), HttpStatusCode.valueOf(404), mock(WebRequest.class));

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = (AppError) response.getBody();
        assertEquals(400, body.getCode());
        assertEquals("bad input type or format, check the input type and format.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }

    @Test
    public void shouldReturnNotAllowedResponse() {
        HttpStatusCodeException exception = new HttpServerErrorException(HttpStatus.METHOD_NOT_ALLOWED);

        ResponseEntity<AppError> response = restExceptionHandler.handleHttpStatusCodeException(exception);

        assertEquals(405, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(405, body.getCode());
        assertEquals("Method not allowed.", body.getReason());
        assertEquals("Method not allowed.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }

    @Test
    public void shouldReturnErrorOnUnexpectedHttpStatusCodeException() {
        HttpStatusCodeException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<AppError> response = restExceptionHandler.handleHttpStatusCodeException(exception);

        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        AppError body = response.getBody();
        assertEquals(500, body.getCode());
        assertEquals("Server error.", body.getReason());
        assertEquals("An unknown error has occurred.", body.getMessage());
        jaxRsDpsLog.error(exception.getMessage(), exception);
    }
}
