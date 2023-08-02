package philo.magicsproutjpa.performance.sample;

import static java.util.Arrays.stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import jakarta.persistence.Persistence;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import philo.magicsproutjpa.core.exception.MimicInnerException;
import philo.magicsproutjpa.core.exception.MimicJpaInnerException;
import philo.magicsproutjpa.core.support.VoidFunction;

/**
 * 캐시를 적용하지 않은 테스트용 클래스입니다.
 */
public abstract class NonCacheMimicJpaRepository<E, K> {

  private static final int ENTITY_TYPE_INDEX = 0;

  private final EntityManager entityManager = createEntityManager();

  public E save(E entity) {
    K id = getIdValue(entity);

    if (isNewEntity(id)) {
      executeInTransaction(() -> entityManager.persist(entity));
    } else {
      executeInTransaction(() -> entityManager.merge(entity));
    }

    return entity;
  }

  public void deleteAll() {
    String deleteQuery = "delete from " + getEntityName();
    executeInTransaction(() -> entityManager.createQuery(deleteQuery).executeUpdate());
  }

  private void executeInTransaction(VoidFunction function) {
    EntityTransaction transaction = entityManager.getTransaction();
    try {
      transaction.begin();
      function.execute(); // Execute Actual Query
      transaction.commit();
    } catch (Exception e) {
      if (transaction.isActive()) {
        transaction.rollback();
      }
      throw new MimicInnerException(e);
    }
  }

  private EntityManager createEntityManager() {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("magic-sprout-jpa");

    return entityManagerFactory.createEntityManager();
  }

  private boolean isNewEntity(K id) {
    return id == null || entityManager.find(getEntityType(), id) == null;
  }

  private K getIdValue(E entity) {
    try {
      Class<E> entityType = getEntityType();
      Field[] fields = entityType.getDeclaredFields();
      Method idGetterMethod = extractIdGetterMethod(entityType, fields);
      return (K) idGetterMethod.invoke(entity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new MimicJpaInnerException(e);
    }
  }

  private Method extractIdGetterMethod(Class<E> entity, Field[] fields) {
    try {
      String idGetterMethodName = extractIdGetterMethodName(fields);
      return entity.getDeclaredMethod(idGetterMethodName);
    } catch (NoSuchMethodException e) {
      throw new MimicJpaInnerException("Id getter method not found", e);
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private String extractIdGetterMethodName(Field[] fields) {
    Field idField = getIdField(fields);
    String idFieldName = idField.getName();
    return getIdGetterMethodName(idFieldName);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private Field getIdField(Field[] fields) {
    return stream(fields)
        .filter(this::hasIdAnnotation)
        .findAny()
        .get();
  }

  private static String getIdGetterMethodName(String idFieldName) {
    return "get" + idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1);
  }

  private boolean hasIdAnnotation(Field field) {
    return field.getAnnotation(Id.class) != null;
  }

  @SuppressWarnings("unchecked")
  private Class<E> getEntityType() {
    ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
    Type[] typeArguments = superclass.getActualTypeArguments();
    Type typeArgument = typeArguments[ENTITY_TYPE_INDEX];
    return (Class<E>) typeArgument;
  }

  private String getEntityName() {
    return getEntityType().getSimpleName();
  }
}
