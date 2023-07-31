package philo.magicsproutjpa;

import jakarta.persistence.EntityManager;

public class ItemRepository extends MimicJpaRepository <Item, Long> {

	public ItemRepository(EntityManager entityManager) {
		super(entityManager);
	}
}
