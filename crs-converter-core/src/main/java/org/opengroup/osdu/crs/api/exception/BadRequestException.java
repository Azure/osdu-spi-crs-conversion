package org.opengroup.osdu.crs.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends HttpStatusCodeException {

    public BadRequestException(String statusText) {
        super(HttpStatus.BAD_REQUEST, statusText);
    }
}
