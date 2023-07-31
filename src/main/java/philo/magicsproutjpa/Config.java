package philo.magicsproutjpa;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Config {

	private final EntityManager entityManager;

	@Bean
	public ItemRepository itemRepository() {
		return new ItemRepository(entityManager);
	}
}
