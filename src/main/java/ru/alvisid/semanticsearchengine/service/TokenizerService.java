package ru.alvisid.semanticsearchengine.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.dto.Tokens;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author EGlushkov
 * Date: 06.07.2026
 * Time: 22:09
 */

@Slf4j
@Service
public class TokenizerService {

    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void init() throws IOException {
        log.info("Загрузка токенизатора...");
        // Загружаем токенизатор из той же модели, что и ONNX
        tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/all-MiniLM-L6-v2");
        log.info("✅ Токенизатор успешно загружен");
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
}
