package com.panda.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BluepayServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(BluepayServiceApplication.class, args);
  }
}
