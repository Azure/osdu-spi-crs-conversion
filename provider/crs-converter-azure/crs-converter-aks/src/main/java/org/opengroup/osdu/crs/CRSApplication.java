package org.opengroup.osdu.crs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import jakarta.inject.Named;

@SpringBootApplication
@ComponentScan({
		"org.opengroup.osdu.crs",
		"org.opengroup.osdu.core",
		"org.opengroup.osdu.azure"
})
@PropertySource("classpath:swagger.properties")
public class CRSApplication extends CRSApplicationBase {

	@Value("${azure.keyvault.url}")
	private String keyVaultURL;

	@Bean
	@Named("KEY_VAULT_URL")
	public String keyVaultURL() {
		return keyVaultURL;
	}

	public static void main(String[] args) {
		SpringApplication.run(CRSApplication.class, args);
	}

}
