package itsm.itsm_backend;

import itsm.itsm_backend.ticket.jpa.UserRepository;
import itsm.itsm_backend.user.Role;
import itsm.itsm_backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableJpaRepositories(basePackages = "itsm.itsm_backend.ticket.jpa")
//@EnableElasticsearchRepositories(basePackages = "itsm.itsm_backend.ticket.elastic")
@RequiredArgsConstructor
public  class ItsmBackendApplication implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    public static void main(String[] args) {
        SpringApplication.run(ItsmBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setFirstname("admin");
            admin.setLastname("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnable(true);
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);


        }
    }
}
