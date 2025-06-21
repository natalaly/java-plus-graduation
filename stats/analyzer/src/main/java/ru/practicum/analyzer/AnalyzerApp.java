package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

//    EventProcessor eventProcessor = context.getBean(EventProcessor.class);
//    final UserProcessor userProcessor = context.getBean(UserProcessor.class);
//
//    Thread userThread = new Thread(userProcessor);
//    userThread.setName("eventHandlerThread");
//    userThread.start();
//
//    eventProcessor.start();
  }
}