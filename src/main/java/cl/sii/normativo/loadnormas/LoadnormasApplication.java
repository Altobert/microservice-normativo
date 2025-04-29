package cl.sii.normativo.loadnormas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class LoadnormasApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoadnormasApplication.class, args);
	}

}
