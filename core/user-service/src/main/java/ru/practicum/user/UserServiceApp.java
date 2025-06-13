package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "ru.practicum.user",
    "ru.practicum.exception.handler"
})
public class UserServiceApp {

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApp.class, args);
  }
}