package ru.alvisid.semanticsearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:31
 */

@Data
@AllArgsConstructor
public class EmbeddingResponse {
    private float[] embedding;
}
