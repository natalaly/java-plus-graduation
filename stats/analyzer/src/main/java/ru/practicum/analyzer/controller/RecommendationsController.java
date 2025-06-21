package ru.practicum.analyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.recommendation.RecommendationsControllerGrpc;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

//  private final UserActionServiceImpl userActionServiceImpl;
//  private final EventSimilarityServiceImpl eventSimilarityServiceImpl;

}
