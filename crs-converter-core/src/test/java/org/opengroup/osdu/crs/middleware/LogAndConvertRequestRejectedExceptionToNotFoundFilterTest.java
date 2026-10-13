package org.opengroup.osdu.crs.middleware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.security.web.firewall.RequestRejectedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class LogAndConvertRequestRejectedExceptionToNotFoundFilterTest {

    @InjectMocks
    private LogAndConvertRequestRejectedExceptionToNotFoundFilter filter;

    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @Test
    public void shouldConvertRequestRejectedExceptionToNotFound() throws IOException{
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        RequestRejectedException exception = new RequestRejectedException("error");


        filter.handle(httpServletRequest, httpServletResponse, exception);

        Mockito.verify(jaxRsDpsLog).error("request_rejected: remote=null, user_agent=null, request_url=null", exception);
        Mockito.verify(httpServletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
