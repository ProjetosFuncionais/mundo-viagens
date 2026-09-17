package br.com.mundoviagens;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
@SpringBootApplication
public class MundoViagensApplication {
    public static void main(String[] args) { SpringApplication.run(MundoViagensApplication.class, args); }
    @Bean Clock clock() { return Clock.systemUTC(); }
}
