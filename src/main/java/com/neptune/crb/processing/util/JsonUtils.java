package com.neptune.crb.processing.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {
    public static String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    public static int getInt(JsonNode node, String field, int defaultValue) {
        return node.has(field) && node.get(field).canConvertToInt() ? node.get(field).asInt() : defaultValue;
    }
}

