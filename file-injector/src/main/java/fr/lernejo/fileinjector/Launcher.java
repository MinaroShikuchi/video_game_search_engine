package fr.lernejo.fileinjector;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lernejo.search.api.Game;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Launcher {

    public static void main(String[] args) {
        try (AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(Launcher.class)) {

            System.out.println("Hello after starting Spring");
            if (args.length > 1) throw new RuntimeException("Wrong number of argument");

            File file = Paths.get(args[0]).toFile();
            if (!file.exists()) throw new FileNotFoundException();

            Game[] games = new ObjectMapper().readValue(file, Game[].class);

            RabbitTemplate template = springContext.getBean(RabbitTemplate.class);

            for (Game game : games) {
                template.setMessageConverter(new Jackson2JsonMessageConverter());
                template.convertAndSend("", "game_info", game, m -> {
                    m.getMessageProperties().setHeader("game_id", game.id);
                    return m;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
