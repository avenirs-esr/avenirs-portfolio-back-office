package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.serializer.ConfigurationValueSerializer;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import org.springframework.stereotype.Component;

@Component
public class JacksonConfigurationValueSerializer implements ConfigurationValueSerializer {
  private final ObjectMapper objectMapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();

  @Override
  public String serialize(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new ConfigurationException(
          "Unable to serialize configuration value: " + e.getOriginalMessage());
    }
  }

  @Override
  public <T> T deserialize(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new ConfigurationException(
          "Unable to read configuration value as "
              + type.getSimpleName()
              + ": "
              + e.getOriginalMessage());
    }
  }
}
