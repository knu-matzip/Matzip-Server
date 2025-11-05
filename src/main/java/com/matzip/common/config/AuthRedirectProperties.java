package com.matzip.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.redirect")
public class AuthRedirectProperties {
    // 콜백 성공 시 리다이렉트할 url
    private String successUri;

    private boolean cookieSecure = true;

    private String cookieSameSite = "Lax";
}
