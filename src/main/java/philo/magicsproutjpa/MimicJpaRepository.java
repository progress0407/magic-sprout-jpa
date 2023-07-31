package philo.magicsproutjpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SimpleJpaRepository 처럼 JPA의 기능을 모방한 클래스입니다.
 *
 * @param <T> the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 */

@RequiredArgsConstructor
@Slf4j
public abstract class MimicJpaRepository<T, ID> {

	private final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("magic-sprout-jpa");
	private final EntityManager entityManager = entityManagerFactory.createEntityManager();

	T save(T entity) {
		Class clazz = getClazz();

		EntityTransaction transaction = entityManager.getTransaction();

		try {
			transaction.begin();
			entityManager.persist(entity);
			transaction.commit();
		}
		catch (Exception e) {
			transaction.rollback();
			log.info("transaction rollback !");
			throw new RuntimeException(e);
		}

		return entity;
	}

	T findById(ID id) {
		Class clazz = getClazz();
		return (T) entityManager.find(clazz, id);
	}

	private Class getClazz() {
		return (Class) getType();
	}

	private Type getType() {
		return ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}
}
