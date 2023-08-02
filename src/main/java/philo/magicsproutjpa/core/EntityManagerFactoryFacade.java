package philo.magicsproutjpa.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerFactoryFacade {

  private static final String PERSISTENCE_UNIT_NAME = "magic-sprout-jpa";

  private static final EntityManagerFactory entityManagerFactory =
      Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

  public static EntityManager createEntityManager() {
    return entityManagerFactory.createEntityManager();
  }
}
