package philo.magicsproutjpa.business.item;

import java.util.List;
import philo.magicsproutjpa.core.MimicJpaRepository;

public class ItemRepository extends MimicJpaRepository<Item, Long> {

  public List<Item> findByName(String name) {
    return invokeQueryMethod(name);
  }
}
