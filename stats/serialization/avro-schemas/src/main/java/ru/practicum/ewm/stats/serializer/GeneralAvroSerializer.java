package ru.practicum.ewm.stats.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

  private final EncoderFactory encoderFactory = EncoderFactory.get();
  private BinaryEncoder encoder;

  @Override
  public byte[] serialize(final String topic, final SpecificRecordBase data) {
    log.debug("Starting serialization for the topic: {}, data: {}.", topic, data);
    if (data == null) {
      log.warn("No data to serialize for topic: {}, data: {}.", topic, data);
      return null;
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] result;
      encoder = encoderFactory.binaryEncoder(out, encoder);
      final DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
      writer.write(data, encoder);
      encoder.flush();
      result = out.toByteArray();
      log.debug("Serialization completed successfully for the topic: {}", topic);
      return result;
    } catch (IOException e) {
      log.error("Error occurred during serialization for topic [{}]: {}", topic, e.getMessage());
      throw new SerializationException(
          "Error occurs during data serialization for the topic [" + topic + "].", e);
    }
  }
}
