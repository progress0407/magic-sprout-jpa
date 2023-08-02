package philo.magicsproutjpa.core.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class EntityManagerFactoryFacade {

  private static final String PERSISTENCE_UNIT_NAME = "magic-sprout-jpa";

  private EntityManagerFactoryFacade() {
    throw new IllegalStateException("Utility class");
  }

  private static final EntityManagerFactory entityManagerFactory =
      Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

  public static EntityManager createEntityManager() {
    return entityManagerFactory.createEntityManager();
  }
}
