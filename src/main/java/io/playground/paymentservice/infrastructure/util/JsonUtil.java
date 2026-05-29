package io.playground.paymentservice.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.paymentservice.exception.BusinessErrorCode;
import io.playground.paymentservice.exception.BusinessErrorDto;
import io.playground.paymentservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtil {
    private final ObjectMapper objectMapper;

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    BusinessErrorCode.JSON_PROCESSING_FAILED
            );
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new BusinessException(
                    BusinessErrorCode.JSON_PROCESSING_FAILED
            );
        }
    }

    public <T> T convert(Object fromValue, TypeReference<T> typeReference) {
        try {
            return objectMapper.convertValue(fromValue, typeReference);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    BusinessErrorCode.JSON_PROCESSING_FAILED
            );
        }
    }

    public boolean isBusinessDetailError(String json) {
        try {
            return objectMapper
                    .readValue(json, BusinessErrorDto.class)
                    .getDetail() != null;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
