package jentus.parser.wordparser;

import jentus.parser.wordparser.services.Runner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ApplicationContext context=SpringApplication.run(App.class, args);
        context.getBean(Runner.class).run();
    }

}
