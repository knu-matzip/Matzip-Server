package com.matzip.common.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "matzip.auth")
public class AuthRedirectProperties {

    private List<String> allowedOrigins;

    private String successPath;          // 예: /auth/callback/success

    private String failurePath;

    private String stateSecret;

    private boolean cookieSecure = true;

    private String cookieSameSite = "Lax";
}
