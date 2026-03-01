package com.matzip;

import com.matzip.common.config.AuthRedirectProperties;
import com.matzip.common.config.DiscordWebhookProperties;
import com.matzip.common.config.JwtProperties;
import com.matzip.common.config.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class,
		AuthRedirectProperties.class,
		KakaoProperties.class,
		DiscordWebhookProperties.class})
public class MatzipServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatzipServerApplication.class, args);
	}

}
