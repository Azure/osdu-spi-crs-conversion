package org.opengroup.osdu.crs.middleware;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogAndConvertRequestRejectedExceptionToNotFoundFilter implements RequestRejectedHandler {

    public static final String REQUEST_REJECTED_REASON_MESSAGE = "request_rejected: remote=%s, user_agent=%s, request_url=%s";

    private final JaxRsDpsLog log;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException requestRejectedException) throws IOException {
        log.error(String.format(REQUEST_REJECTED_REASON_MESSAGE,
                request.getRemoteHost(), request.getHeader(HttpHeaders.USER_AGENT), request.getRequestURL()), requestRejectedException);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}

