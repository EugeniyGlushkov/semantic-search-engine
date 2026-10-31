package ru.alvisid.semanticsearchengine.worker.service;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class TokenizerService {

    @Value("${ml.model.embedding.tokenizer.path}")
    private Resource embeddingTokenizerResource;

    @Value("${ml.model.cross-encoder.tokenizer.path}")
    private Resource crossEncoderTokenizerResource;

    private HuggingFaceTokenizer embeddingTokenizer;
    private HuggingFaceTokenizer crossEncoderTokenizer;

    @PostConstruct
    public void init() {
        try {
            this.embeddingTokenizer = HuggingFaceTokenizer.newInstance(embeddingTokenizerResource.getFilePath());

            log.info("Токенизатор эмбеддингов успешно инициализирован: {}", embeddingTokenizerResource.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать HuggingFace токенизатор эмбеддингов", e);
        }


        try {
            this.crossEncoderTokenizer = HuggingFaceTokenizer.newInstance(crossEncoderTokenizerResource.getFilePath());

            log.info("Токенизатор кроссэнкодера успешно инициализирован: {}", crossEncoderTokenizerResource.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать HuggingFace токенизатор кроссэнкодера", e);
        }
    }

    /**
     * Преобразует текст в input_ids и attention_mask для модели.
     *
     * @param text Входной текст
     * @return Массив из двух элементов: [input_ids, attention_mask]
     */
    public Tokens tokenize(String text) {
        // Токенизируем текст без ограничения длины (или с ограничением через параметры)
        log.debug("Токенизация текста: {}", text);
        Encoding encoding = embeddingTokenizer.encode(text);
        return getTokens(encoding);
    }

    public Tokens tokenizePair(String query, String document) {
        // Токенизируем пару: [CLS] query [SEP] document [SEP]
        log.debug("Токенизация пары: [CLS] {} [SEP] {} [SEP]", query, document);
        Encoding encoding = crossEncoderTokenizer.encode(query, document);
        return getTokens(encoding);
    }

    private Tokens getTokens(Encoding encoding) {
        // Получаем ID токенов (input_ids)
        long[] inputIds = encoding.getIds();
        // Получаем маску внимания (1 для реальных токенов, 0 для PAD)
        long[] attentionMask = encoding.getAttentionMask();
        long[] tokenTypeIds = encoding.getTypeIds();

        log.debug("Сгенерировано {} токенов", inputIds.length);
        log.debug("Первые 5 ID: {}", Arrays.toString(Arrays.copyOf(inputIds, Math.min(5, inputIds.length))));
        log.debug("Первые 5 Type ID: {}", Arrays.toString(Arrays.copyOf(tokenTypeIds, Math.min(5, tokenTypeIds.length))));

        return new Tokens(inputIds, attentionMask, tokenTypeIds);
    }

    @PreDestroy
    public void close() {
        // Закрываем токенизатор
        if (embeddingTokenizer != null) {
            embeddingTokenizer.close();
        }

        if (crossEncoderTokenizer != null) {
            crossEncoderTokenizer.close();
        }
    }
}