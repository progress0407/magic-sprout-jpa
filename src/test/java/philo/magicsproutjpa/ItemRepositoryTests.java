package philo.magicsproutjpa;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ItemRepositoryTests {

	ItemRepository itemRepository = new ItemRepository();

	@DisplayName("[save, findById] 저장하고 불러올 수 있어야 한다")
	@Test
	void save_findById() {
		// given
		Item item = new Item("black jean");

		// when
		itemRepository.save(item);
		Item foundItem = itemRepository.findById(item.getId());

		// then
		assertAll(
				() -> assertThat(foundItem.getId()).isNotNull(),
				() -> assertThat(foundItem.getName()).isEqualTo("black jean")
		);
	}

	@DisplayName("[findAll] 모두 불러올 수 있어야 한다")
	@Test
	void findAll() {
		Item item = new Item("black jean");
		Item item2 = new Item("black jean2");
		itemRepository.save(item);
		itemRepository.save(item2);

		List<Item> all = itemRepository.findAll();
		System.out.println("all = " + all);
	}

}