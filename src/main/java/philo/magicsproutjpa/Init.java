package philo.magicsproutjpa;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class Init {

	private final EntityManager entityManager;

	private final ItemRepository itemRepository;

	@PostConstruct
	@Transactional
	public void init() {
		entityManager.persist(new Item("black jean"));

		Item item = itemRepository.findById(1L);

		System.out.println(item);
	}
}
