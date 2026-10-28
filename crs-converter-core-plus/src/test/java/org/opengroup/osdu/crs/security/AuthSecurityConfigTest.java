package org.opengroup.osdu.crs.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.crs.middleware.AuthenticationService;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

class AuthSecurityConfigTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withInitializer(
              context ->
                  context
                      .getBeanFactory()
                      .registerSingleton(
                          "authenticationService", mock(AuthenticationService.class)))
          .withUserConfiguration(AuthSecurityConfig.class);

  @Test
  void securityConfigurationLoadsWithMethodSecurityEnabled() {
    contextRunner.run(
        context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(SecurityFilterChain.class);
        });
  }
}
