package xyz.eneroth.memo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Memo {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Memo.class, args);
    }
}