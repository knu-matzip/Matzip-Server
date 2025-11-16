package com.matzip.auth.application.util;

import com.matzip.common.config.AuthRedirectProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class StatelessStateSignerTest {

    private static final Logger log = LoggerFactory.getLogger(StatelessStateSignerTest.class);

    private StatelessStateSigner statelessStateSigner;

    @BeforeEach
    void setUp() {
        AuthRedirectProperties properties = new AuthRedirectProperties(
                List.of("http://localhost:3000", "https://knu-matzip-dev.vercel.app"),
                "/login/loading/success",
                "/login/loading/fail",
                "test-state-secret",
                false,
                "None"
        );

        this.statelessStateSigner = new StatelessStateSigner(properties);
    }

    @Test
    void createState() {
        String origin = "origin";
        String signedState = statelessStateSigner.createSignedState(origin);
        log.info("signedState: {}", signedState);

        String verifiedOrigin = statelessStateSigner.verifyAndGetOrigin(signedState);
        log.info("verifiedOrigin: {}", verifiedOrigin);
    }
}
