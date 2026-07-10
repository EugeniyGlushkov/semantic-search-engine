package ru.alvisid.semanticsearchengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class SemanticSearchEngineApplicationTests {

    @Test
    void contextLoads() {
    }

}
