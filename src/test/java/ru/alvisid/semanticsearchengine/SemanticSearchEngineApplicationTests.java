package ru.alvisid.semanticsearchengine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
@Disabled("Временно отключаем для деплоя в k8s, так как h2 не поддерживает vector")
class SemanticSearchEngineApplicationTests {

    @Test
    void contextLoads() {
    }

}
