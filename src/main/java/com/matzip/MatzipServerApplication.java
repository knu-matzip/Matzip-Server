package com.matzip;

import com.matzip.common.config.AuthRedirectProperties;
import com.matzip.common.config.JwtProperties;
import com.matzip.common.config.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class,
		AuthRedirectProperties.class,
		KakaoProperties.class})
public class MatzipServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatzipServerApplication.class, args);
	}

}
