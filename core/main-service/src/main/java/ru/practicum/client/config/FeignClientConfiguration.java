package ru.practicum.client.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.exception.FeignErrorDecoder;

@Configuration
public class FeignClientConfiguration {

  @Bean
  public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoder();
  }

}
