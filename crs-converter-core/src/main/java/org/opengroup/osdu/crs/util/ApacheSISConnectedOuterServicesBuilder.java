package org.opengroup.osdu.crs.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sis.util.Version;
import org.opengroup.osdu.core.common.info.ConnectedOuterServicesBuilder;
import org.opengroup.osdu.core.common.model.info.ConnectedOuterService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnMissingBean(type = "ConnectedOuterServicesBuilder")
@Slf4j
@RequestScope
public class ApacheSISConnectedOuterServicesBuilder implements ConnectedOuterServicesBuilder {

  private static final String APACHE_SIS_PREFIX = "Apache SIS-";

  @Override
  public List<ConnectedOuterService> buildConnectedOuterServices() {
    return Stream.of(fetchApacheSisInfo())
        .collect(Collectors.toList());
  }

  private ConnectedOuterService fetchApacheSisInfo() {
    return ConnectedOuterService.builder()
        .name(APACHE_SIS_PREFIX + StringUtils.substringAfterLast(Version.class.getName(), "."))
        .version(Version.SIS.toString())
        .build();
  }

}