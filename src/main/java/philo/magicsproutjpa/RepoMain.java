package philo.magicsproutjpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class RepoMain {

	public static void main(String[] args) {

		ItemRepository itemRepository = new ItemRepository();

		Item item = new Item("black jean");

		itemRepository.save(item);

		Item foundItem = itemRepository.findById(item.getId());

		System.out.println("foundItem = " + foundItem);
	}
}
