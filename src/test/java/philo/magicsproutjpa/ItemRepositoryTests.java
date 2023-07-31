package philo.magicsproutjpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ItemRepositoryTests {

	@Test
	void 저장하고_불러올_수_있어야_한다() {
		// given
		Item item = new Item("black jean");
		ItemRepository itemRepository = new ItemRepository();

		// when
		itemRepository.save(item);
		Item foundItem = itemRepository.findById(item.getId());

		// then
		assertAll(
				() -> assertThat(foundItem.getId()).isNotNull(),
				() -> assertThat(foundItem.getName()).isEqualTo("black jean")
		);
	}
}