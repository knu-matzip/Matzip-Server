package com.matzip.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthRedirectProperties {

    private final List<String> allowedOrigins;

    private final String successPath;

    private final String failurePath;

    private final String stateSecret;

    private final boolean cookieSecure;

    private final String cookieSameSite;

    public AuthRedirectProperties(List<String> allowedOrigins, String successPath,
                                  String failurePath, String stateSecret,
                                  boolean cookieSecure, String cookieSameSite) {
        this.allowedOrigins = allowedOrigins;
        this.successPath = successPath;
        this.failurePath = failurePath;
        this.stateSecret = stateSecret;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }
}
