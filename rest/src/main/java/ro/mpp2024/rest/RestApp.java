package ro.mpp2024.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import ro.mpp2024.repository.CursaHibernateRepository;
import ro.mpp2024.repository.CursaRepository;

// Dezactivam auto-configurarea JPA (folosim Hibernate direct)
@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class RestApp {

    public static void main(String[] args) {
        SpringApplication.run(RestApp.class, args);
    }

    // Bean-ul repository pe care il injecteaza Spring in controller
    @Bean
    public CursaRepository cursaRepository() {
        return new CursaHibernateRepository();
    }
}