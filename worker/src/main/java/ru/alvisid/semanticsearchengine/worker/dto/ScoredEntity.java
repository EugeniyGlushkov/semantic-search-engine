package ru.alvisid.semanticsearchengine.worker.dto;

import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;

public record ScoredEntity(EmbeddingEntity entity, float score) {
}
