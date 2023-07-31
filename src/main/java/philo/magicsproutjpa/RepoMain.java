package philo.magicsproutjpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class RepoMain {

	public static void main(String[] args) {

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("magic-sprout-jpa");
		EntityManager entityManager = entityManagerFactory.createEntityManager();


		Item item = new Item("black jean");

		System.out.println("before save:: item.getId() = " + item.getId());

		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(item);
		transaction.commit();

		System.out.println("after save:: item.getId() = " + item.getId());
	}
}
