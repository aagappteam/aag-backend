package aagapp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"aagapp_backend.repository"})
@EntityScan(basePackages = {"aagapp_backend.entity"})
public class AagappBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AagappBackendApplication.class, args);
	}

}
