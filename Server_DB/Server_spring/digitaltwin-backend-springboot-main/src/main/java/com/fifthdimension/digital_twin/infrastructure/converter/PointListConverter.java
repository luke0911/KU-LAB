package com.fifthdimension.digital_twin.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifthdimension.digital_twin.global.entity.Point;
import com.fifthdimension.digital_twin.global.exception.CustomException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

@Slf4j
@Converter
public class PointListConverter implements AttributeConverter<List<Point>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Point> pointList) {
        try {
            return objectMapper.writeValueAsString(pointList);
        } catch (JsonProcessingException e) {
            log.error("Point List JSON 변환 실패: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Point List JSON 변환 실패: " + e.getMessage());
        }
    }

    @Override
    public List<Point> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(List.class, Point.class));
        } catch (IOException e) {
            log.error("JSON으로부터 Point List 변환 실패: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON으로부터 Point List 변환 실패: " + e.getMessage());
        }
    }
}