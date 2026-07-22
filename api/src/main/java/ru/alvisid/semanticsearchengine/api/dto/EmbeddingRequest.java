package ru.alvisid.semanticsearchengine.api.dto;

import lombok.Data;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:30
 */

@Data
public class EmbeddingRequest {
    private String text;
}