package picky.parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import picky.parser.dto.SourcePage;
import picky.parser.dto.TestSource;

import java.io.IOException;
import java.util.List;

public class UtilObjectMapper {
    
    private ObjectMapper om = new ObjectMapper();

    public UtilObjectMapper() {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<TestSource> readTestSources(String data) {
        try {
            return om.readValue(
                data,
                new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("invalid data");
        }
    }

    public List<SourcePage> readSourcePages(String data) {
        try {
            return om.readValue(data, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("invalid data");
        }
    }

    public <T> T read(byte[] data, Class<T> zz) {
        try {
            return om.readValue(data, zz);
        } catch (IOException e) {
            throw new IllegalStateException("invalid data");
        }
    }

    public String writeString(Object value) {
        try {
            return om.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("invalid data");
        }
    }

    public byte[] writeData(Object value) {
        try {
            return om.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("invalid data");
        }
    }
}
