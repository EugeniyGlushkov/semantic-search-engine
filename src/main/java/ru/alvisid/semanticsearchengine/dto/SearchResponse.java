package ru.alvisid.semanticsearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author EGlushkov
 * Date: 14.07.2026
 * Time: 17:41
 */

@Data
@AllArgsConstructor
public class SearchResponse {
    List<String> texts;
}
