package com.neptune.crb.processing.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class JsonDeserializer
{
    public ResponseWrapper deserializeJson(String jsonString) throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper.readValue(jsonString, ResponseWrapper.class);
    }
}
