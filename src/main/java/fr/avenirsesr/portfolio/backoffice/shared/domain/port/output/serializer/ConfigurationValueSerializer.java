package fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.serializer;

public interface ConfigurationValueSerializer {
  String serialize(Object value);

  <T> T deserialize(String json, Class<T> type);
}
