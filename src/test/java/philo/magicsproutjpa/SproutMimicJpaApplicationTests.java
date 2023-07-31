package philo.magicsproutjpa;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SproutMimicJpaApplicationTests {

	@Autowired
	ItemRepository itemRepository;

	@Test
	void contextLoads() {
		itemRepository.findById(1L);
	}
}
