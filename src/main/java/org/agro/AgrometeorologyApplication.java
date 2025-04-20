package org.agro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgrometeorologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrometeorologyApplication.class, args);
    }

}
