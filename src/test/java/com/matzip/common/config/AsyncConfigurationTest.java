package com.matzip.common.config;

import com.matzip.AbstractMatzipApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigurationTest extends AbstractMatzipApplicationTest {

    @Autowired
    @Qualifier("generalExecutor")
    private ThreadPoolTaskExecutor generalExecutor;

    @Autowired
    @Qualifier("externalExecutor")
    private ThreadPoolTaskExecutor externalExecutor;

    @Test
    void 내부작업_풀과_외부IO_풀이_분리되어_등록된다() {
        assertThat(generalExecutor).isNotSameAs(externalExecutor);
    }

    @Test
    void generalExecutor는_Hikari_커넥션을_고려한_크기로_구성된다() {
        assertThat(generalExecutor.getCorePoolSize()).isEqualTo(3);
        assertThat(generalExecutor.getMaxPoolSize()).isEqualTo(4);
    }

    @Test
    void externalExecutor는_저빈도_외부IO용_작은_크기로_구성된다() {
        assertThat(externalExecutor.getCorePoolSize()).isEqualTo(1);
        assertThat(externalExecutor.getMaxPoolSize()).isEqualTo(2);
    }
}
