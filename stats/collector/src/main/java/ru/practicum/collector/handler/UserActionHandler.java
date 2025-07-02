package ru.practicum.collector.handler;

import ru.practicum.ewm.stats.action.UserActionProto;

public interface UserActionHandler {

  void handle(UserActionProto userAction);
}
