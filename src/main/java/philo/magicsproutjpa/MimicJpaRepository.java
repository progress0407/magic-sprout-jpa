package philo.magicsproutjpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MimicJpaRepository<T, ID> {

	private final EntityManager entityManager;

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
