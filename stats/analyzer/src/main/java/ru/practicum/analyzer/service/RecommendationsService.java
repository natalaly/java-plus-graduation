package ru.practicum.analyzer.service;

import java.util.List;
import ru.practicum.ewm.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendation.UserPredictionsRequestProto;

public interface RecommendationsService {

  List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);

  List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

  List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto similarEventsRequestProto);
}
