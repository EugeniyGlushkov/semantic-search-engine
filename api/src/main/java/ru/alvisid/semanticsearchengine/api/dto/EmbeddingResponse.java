package ru.alvisid.semanticsearchengine.api.dto;

import lombok.Data;

/**
 * @author EGlushkov
 * Date: 22.07.2026
 * Time: 21:14
 */

@Data
public class EmbeddingResponse {
    private String text;
    private float[] embedding;
}