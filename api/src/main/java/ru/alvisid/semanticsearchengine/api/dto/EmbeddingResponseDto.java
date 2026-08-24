package ru.alvisid.semanticsearchengine.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author EGlushkov
 * Date: 22.07.2026
 * Time: 21:14
 */

@Data
@Builder
@Accessors(chain=true)
public class EmbeddingResponseDto {
    private String text;
    private List<Float> embedding;
}