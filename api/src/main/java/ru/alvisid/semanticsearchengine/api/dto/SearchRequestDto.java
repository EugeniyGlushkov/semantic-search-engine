package ru.alvisid.semanticsearchengine.api.dto;

import lombok.Data;

/**
 * @author EGlushkov
 * Date: 08.07.2026
 * Time: 14:20
 */

@Data
public class SearchRequestDto {
    private String query;
    private int topK;
    private boolean rerank = false;
}