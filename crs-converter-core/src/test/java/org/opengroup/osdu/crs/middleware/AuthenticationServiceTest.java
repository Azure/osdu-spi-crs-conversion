package org.opengroup.osdu.crs.middleware;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.powermock.reflect.Whitebox;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Before
    public void init() {
        Whitebox.setInternalState(authenticationService, "entitlementsUrl", "entitlementsUrl");
    }

    @Test
    public void shouldHandleEntitlementsException() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);

        authenticationService.initEntitlementsFactory();
        boolean result = authenticationService.isAuthorized(httpServletRequest, httpServletResponse);

        Assert.assertFalse(result);
        Mockito.verify(jaxRsDpsLog).error("User not authenticated. Response: HttpResponse(headers=null," +
                " body=, contentType=, responseCode=0, exception=org.apache.http.client.ClientProtocolException," +
                " request=entitlementsUrl/groups, httpMethod=GET, latency=0)");
        Mockito.verify(handlerExceptionResolver).resolveException(Mockito.eq(httpServletRequest),
                Mockito.eq(httpServletResponse), Mockito.eq(null), Mockito.any(AppException.class));
    }

    @Test
    public void shouldHandleNPEFromEntitlementsService() throws EntitlementsException {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        EntitlementsFactory entitlementsFactory = Mockito.mock(EntitlementsFactory.class);
        Whitebox.setInternalState(authenticationService, "entitlementsFactory", entitlementsFactory);
        IEntitlementsService entitlementsService = Mockito.mock(IEntitlementsService.class);
        Mockito.when(entitlementsFactory.create(Mockito.any(DpsHeaders.class))).thenReturn(entitlementsService);
        Mockito.when(entitlementsService.getGroups()).thenThrow(new NullPointerException());

        boolean result = authenticationService.isAuthorized(httpServletRequest, httpServletResponse);

        Assert.assertFalse(result);
        Mockito.verify(jaxRsDpsLog).error("User not authenticated. Null pointer exception: null");
        Mockito.verify(handlerExceptionResolver).resolveException(Mockito.eq(httpServletRequest),
                Mockito.eq(httpServletResponse), Mockito.eq(null), Mockito.any(AppException.class));
    }

    @Test
    public void shouldVerifyAuthenticationSuccessfully() throws EntitlementsException {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        Enumeration<String> headerNames = Mockito.mock(Enumeration.class);
        Mockito.when(headerNames.hasMoreElements()).thenReturn(false);
        Mockito.when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        EntitlementsFactory entitlementsFactory = Mockito.mock(EntitlementsFactory.class);
        Whitebox.setInternalState(authenticationService, "entitlementsFactory", entitlementsFactory);
        IEntitlementsService entitlementsService = Mockito.mock(IEntitlementsService.class);
        Mockito.when(entitlementsFactory.create(Mockito.any(DpsHeaders.class))).thenReturn(entitlementsService);
        Groups groups = new Groups();
        groups.setMemberEmail("email");
        Mockito.when(entitlementsService.getGroups()).thenReturn(groups);

        boolean result = authenticationService.isAuthorized(httpServletRequest, httpServletResponse);

        Assert.assertTrue(result);
        Mockito.verify(jaxRsDpsLog).debug("User authenticated | User: email");
        Mockito.verifyNoMoreInteractions(handlerExceptionResolver);
    }
}
