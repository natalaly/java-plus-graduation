package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {
    "ru.practicum.comment",
    "ru.practicum.exception.handler"
})
public class CommentServiceApp {

  public static void main(String[] args) {
    SpringApplication.run(CommentServiceApp.class, args);
  }
}