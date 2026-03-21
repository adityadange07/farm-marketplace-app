package com.farmmarket.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Generic JSON ↔ String converter for MySQL JSON columns
 */
public class JsonConverter {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ── Map<String, Object> Converter ──────────
    @Converter
    public static class MapConverter
            implements AttributeConverter<Map<String, Object>, String> {

        @Override
        public String convertToDatabaseColumn(Map<String, Object> attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize JSON", e);
            }
        }

        @Override
        public Map<String, Object> convertToEntityAttribute(String dbData) {
            try {
                return dbData != null
                        ? mapper.readValue(dbData, new TypeReference<>() {})
                        : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize JSON", e);
            }
        }
    }

    // ── Map<String, String> Converter ──────────
    @Converter
    public static class StringMapConverter
            implements AttributeConverter<Map<String, String>, String> {

        @Override
        public String convertToDatabaseColumn(Map<String, String> attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize JSON", e);
            }
        }

        @Override
        public Map<String, String> convertToEntityAttribute(String dbData) {
            try {
                return dbData != null
                        ? mapper.readValue(dbData, new TypeReference<>() {})
                        : null;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize JSON", e);
            }
        }
    }

    // ── List<Map<String, Object>> Converter ────
    @Converter
    public static class ListMapConverter
            implements AttributeConverter<List<Map<String, Object>>, String> {

        @Override
        public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : "[]";
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize JSON", e);
            }
        }

        @Override
        public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
            try {
                return dbData != null
                        ? mapper.readValue(dbData, new TypeReference<>() {})
                        : Collections.emptyList();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize JSON", e);
            }
        }
    }

    // ── List<Map<String, String>> Converter ────
    @Converter
    public static class ListStringMapConverter
            implements AttributeConverter<List<Map<String, String>>, String> {

        @Override
        public String convertToDatabaseColumn(List<Map<String, String>> attribute) {
            try {
                return attribute != null ? mapper.writeValueAsString(attribute) : "[]";
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize JSON", e);
            }
        }

        @Override
        public List<Map<String, String>> convertToEntityAttribute(String dbData) {
            try {
                return dbData != null
                        ? mapper.readValue(dbData, new TypeReference<>() {})
                        : Collections.emptyList();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize JSON", e);
            }
        }
    }
}