package ru.alvisid.semanticsearchengine;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.alvisid.semanticsearchengine.service.TokenizerService;

import static org.mockito.Mockito.mock;

/**
 * @author EGlushkov
 * Date: 09.07.2026
 * Time: 20:27
 */

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public OrtSession mockOrtSession() throws Exception {
        return mock(OrtSession.class);
    }

    @Bean
    @Primary
    public OrtEnvironment mockOrtEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    @Primary
    public TokenizerService mockTokenizerService() {
        return mock(TokenizerService.class);
    }
}
