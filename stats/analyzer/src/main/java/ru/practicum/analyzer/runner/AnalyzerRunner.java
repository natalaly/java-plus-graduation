package ru.practicum.analyzer.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.consumer.UserActionConsumer;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
// TODO uncommented when ready
  final UserActionConsumer userActionConsumer;
//  final EventSimilarityConsumer eventSimilarityConsumer;

  @Override
  public void run(String... args) throws Exception {
// TODO uncommented when ready
//    Thread eventSimilarity = new Thread(eventSimilarityConsumer);
//    userAction.setName("EventSimilarityThread");
//    userAction.start();
//// TODO uncommented when ready
    userActionConsumer.run();
  }

}
