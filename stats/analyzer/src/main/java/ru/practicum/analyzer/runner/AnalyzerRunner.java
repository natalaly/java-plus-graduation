package ru.practicum.analyzer.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.consumer.EventSimilarityConsumer;
import ru.practicum.analyzer.consumer.UserActionConsumer;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

  final UserActionConsumer userActionConsumer;
  final EventSimilarityConsumer eventSimilarityConsumer;

  @Override
  public void run(String... args) throws Exception {

    Thread eventSimilarity = new Thread(eventSimilarityConsumer);
    eventSimilarity.setName("EventSimilarityThread");
    eventSimilarity.start();

    userActionConsumer.run();
  }
}
