package ru.alvisid.semanticsearchengine.worker.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.worker.dto.Tokens;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author EGlushkov
 * Date: 06.07.2026
 * Time: 22:09
 */

@Slf4j
@Service
@ConditionalOnProperty(name = "ml.tokenizer.enabled", havingValue = "true", matchIfMissing = true)
public class TokenizerService {

    @Value("${ml.model.tokenizer.path}")
    private Resource resourcePath;

    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            this.tokenizer = HuggingFaceTokenizer.newInstance(resourcePath.getFilePath());

            System.out.println("Токенизатор успешно инициализирован: " + resourcePath.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать HuggingFace токенизатор", e);
        }
    }

    /**
     * Преобразует текст в input_ids и attention_mask для модели.
     *
     * @param text Входной текст
     * @return Массив из двух элементов: [input_ids, attention_mask]
     */
    public Tokens tokenize(String text) {
        log.debug("Токенизация текста: {}", text);

        // Токенизируем текст без ограничения длины (или с ограничением через параметры)
        Encoding encoding = tokenizer.encode(text);

        // Получаем ID токенов (input_ids)
        long[] inputIds = encoding.getIds();
        // Получаем маску внимания (1 для реальных токенов, 0 для PAD)
        long[] attentionMask = encoding.getAttentionMask();

        log.debug("Сгенерировано {} токенов", inputIds.length);
        log.debug("Первые 5 ID: {}", Arrays.toString(Arrays.copyOf(inputIds, Math.min(5, inputIds.length))));

        return new Tokens(inputIds, attentionMask);
    }

    @PreDestroy
    public void close() throws Exception {
        // Закрываем токенизатор
        if (tokenizer != null) {
            tokenizer.close();
        }
    }
}