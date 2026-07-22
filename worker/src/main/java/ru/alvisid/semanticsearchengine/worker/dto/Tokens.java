package ru.alvisid.semanticsearchengine.worker.dto;

public record Tokens(long[] inputIds, long[] attentionMask) {
}