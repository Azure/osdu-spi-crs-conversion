/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
 
package org.opengroup.osdu.crs.security;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private static Logger filterLogger = Logger.getLogger(AuthenticationRequestFilter.class.getName());

    private final String entitlementsUrl;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final HttpResponseBodyMapper httpResponseBodyMapper;

    public AuthenticationRequestFilter(@Value("${osdu.entitlement.url}") String entitlementsUrl,
                                       HandlerExceptionResolver handlerExceptionResolver) {
        this.entitlementsUrl = entitlementsUrl;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.httpResponseBodyMapper = new HttpResponseBodyMapper(new HttpConfiguration().jsonObjectMapper());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        HttpHeaders requestHeaders = httpHeaders(httpServletRequest);
        DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(requestHeaders.headerSet());
        dpsHeaders.addCorrelationIdIfMissing();
        IEntitlementsFactory factory = getEntitlementsFactory();
        IEntitlementsService service = factory.create(dpsHeaders);
        try {
            Groups groups = service.getGroups();
            String message = String.format("User authenticated | User: %s", groups.getMemberEmail());
            filterLogger.info(message);
            putAuthenticationIntoContext(groups);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (EntitlementsException e) {
            String message = String.format(String.format("User not authenticated. Response: %s", e.getHttpResponse()), e);
            filterLogger.warning(message);
            AppException unauthorized =  AppException.createUnauthorized("Error: " + e.getMessage());
            handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
        }
        catch (NullPointerException e) { // Common library throws null pointer exception when auth permission is denied.
            String message = String.format("User not authenticated. Null pointer exception: %s", e.getMessage());
            filterLogger.warning(message);
            AppException unauthorized = AppException.createUnauthorized("Error: " + e.getMessage());
            handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
        }
    }

    private HttpHeaders httpHeaders(@NonNull HttpServletRequest httpRequest) {
        // Spring 7 HttpHeaders is no longer a Map, so it can't be the Collectors.toMap target
        HttpHeaders headers = new HttpHeaders();
        Collections.list(httpRequest.getHeaderNames())
                .forEach(h -> headers.put(h, Collections.list(httpRequest.getHeaders(h))));
        return headers;
    }

    protected IEntitlementsFactory getEntitlementsFactory() {
        return new EntitlementsFactory(EntitlementsAPIConfig.builder().rootUrl(entitlementsUrl).build(), httpResponseBodyMapper);
    }

    private void putAuthenticationIntoContext(Groups groups) {
        AuthenticationToken authentication = new AuthenticationToken(groups, Collections.emptyList());
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
