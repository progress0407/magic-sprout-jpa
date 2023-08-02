package philo.magicsproutjpa.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import philo.magicsproutjpa.domain.Item;
import philo.magicsproutjpa.domain.ItemRepository;

class ItemRepositoryTest {

  static ItemRepository itemRepository = new ItemRepository();

  @AfterEach
  void tearDown() {
    itemRepository.deleteAll();
  }

  @DisplayName("[save, findById] 리포지토리는 엔티티를 저장하고 불러올 수 있다")
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


  @DisplayName("[save - update] 리포지토리는 엔티티를 저장하고 불러올 수 있다")
  @Test
  void update() {
    // given
    Item item = new Item("black jean");
    itemRepository.save(item);

    // when
    item.changeName("blue jean");
    itemRepository.save(item);

    // then
    Item foundItem = itemRepository.findById(item.getId());

    assertThat(foundItem.getName()).isEqualTo("blue jean");
  }


  @DisplayName("[findAll] 리포지토리는 엔티티를 모두 불러올 수 있다")
  @Test
  void findAll() {
    // given
    Item item1 = new Item("black jean1");
    Item item2 = new Item("black jean2");
    itemRepository.save(item1);
    itemRepository.save(item2);

    // when
    List<Item> all = itemRepository.findAll();

    // then
    assertAll(
        () -> assertThat(all).hasSize(2),
        () -> assertThat(all.stream().map(Item::getName)).containsExactlyInAnyOrder("black jean1",
            "black jean2")
    );
  }

  @DisplayName("[deleteAll] 리포지토리는 엔티티를 모두 정상적으로 삭제할 수 있다")
  @Test
  void deleteAll() {
    // given
    Item item1 = new Item("black jean1");
    Item item2 = new Item("black jean2");
    itemRepository.save(item1);
    itemRepository.save(item2);

    // when
    itemRepository.deleteAll();

    // then
    List<Item> all = itemRepository.findAll();

    assertThat(all).isEmpty();
  }


  @DisplayName("[delete] 리포지토리는 ID로 엔티티를 삭제할 수 있다")
  @Test
  void delete() {
    // given
    Item item = new Item("black jean");
    itemRepository.save(item);

    // when
    itemRepository.deleteById(item.getId());

    // then
    List<Item> all = itemRepository.findAll();

    assertThat(all).isEmpty();
  }


  @DisplayName("[count] 리포지토리는 record 개수를 조회할 수 있다")
  @Test
  void count() {
    // given
    Item item1 = new Item("black jean");
    Item item2 = new Item("black jean2");
    Item item3 = new Item("black jean3");
    itemRepository.save(item1);
    itemRepository.save(item2);
    itemRepository.save(item3);

    // when
    long recordCount = itemRepository.count();

    // then
    assertThat(recordCount).isEqualTo(3);
  }
}