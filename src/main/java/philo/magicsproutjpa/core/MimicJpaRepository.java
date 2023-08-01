package philo.magicsproutjpa.core;

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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import philo.magicsproutjpa.core.exception.MimicJpaCrudException;
import philo.magicsproutjpa.core.exception.MimicJpaInnerException;
import philo.magicsproutjpa.core.support.VoidFunction;

/**
 * SimpleJpaRepository 처럼 JPA의 기능을 모방한 클래스입니다.
 *
 * @param <T>  the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 */

@Slf4j
public abstract class MimicJpaRepository<T, ID> {

  private static final int ENTITY_TYPE_INDEX = 0;

  private final EntityManager entityManager = createEntityManager();

  protected MimicJpaRepository() {
    Class<T> entityType = getEntityType();

    Field[] fields = entityType.getDeclaredFields();

    assertIdFieldExists(fields);
    assertIdGetterExists(entityType, fields);
  }

  public T save(T entity) {
    ID id = getIdValue(entity);

    if (isNewEntity(id)) {
      executeInTransaction(() -> entityManager.persist(entity));
    } else {
      executeInTransaction(() -> entityManager.merge(entity));
    }

    return entity;
  }


  public List<T> findAll() {
    String selectQuery = "select e from " + getEntityName() + " e";

    return entityManager
        .createQuery(selectQuery, getEntityType())
        .getResultList();
  }

  public T findById(ID id) {
    Class<T> entityType = getEntityType();

    return entityManager.find(entityType, id);
  }

  public void deleteAll() {
    String deleteQuery = "delete from " + getEntityName();

    executeInTransaction(() -> entityManager.createQuery(deleteQuery).executeUpdate());
  }

  public void delete(ID id) {
    T entity = entityManager.find(getEntityType(), id);

    executeInTransaction(() -> entityManager.remove(entity));
  }

  public long count() {
    String countQuery = "select count(e) from " + getEntityName() + " e";

    return entityManager
        .createQuery(countQuery, Long.class)
        .getSingleResult();
  }

  private void executeInTransaction(VoidFunction function) {
    EntityTransaction transaction = null;
    try {
      transaction = entityManager.getTransaction();
      transaction.begin();

      // Execute Actual Query
      function.execute();

      transaction.commit();
    } catch (Exception e) {
      if (transaction.isActive()) {
        transaction.rollback();
        log.info("transaction rollback !");
      }
      throw new MimicJpaCrudException(e);
    }
  }


  private EntityManager createEntityManager() {
    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("magic-sprout-jpa");

    return entityManagerFactory.createEntityManager();
  }


  private boolean isNewEntity(ID id) {
    return id == null || entityManager.find(getEntityType(), id) == null;
  }

  private ID getIdValue(T entity) {
    Class<T> entityType = getEntityType();
    Field[] fields = entityType.getDeclaredFields();
    Field idField = stream(fields)
        .filter(this::hasIdAnnotation)
        .findAny()
        .get();
    Method idGetterMethod = extractIdGetterMethod(entityType, fields);
    try {
      return (ID) idGetterMethod.invoke(entity);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private void assertIdFieldExists(Field[] fields) {
    long idFieldCount = getIdFieldCount(fields);

    if (idFieldCount == 0) {
      throw new MimicJpaInnerException("Id field not found");

    } else if (idFieldCount > 1) {
      throw new MimicJpaInnerException("Multiple Id fields found");
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private void assertIdGetterExists(Class<T> entity, Field[] fields) {
    extractIdGetterMethod(entity, fields);
  }

  private Method extractIdGetterMethod(Class<T> entity, Field[] fields) {
    Field idField = stream(fields)
        .filter(this::hasIdAnnotation)
        .findAny()
        .get();

    String idFieldName = idField.getName();

    String idGetterMethodName = "get" + idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1);

    try {
      return entity.getDeclaredMethod(idGetterMethodName);
    } catch (NoSuchMethodException e) {
      throw new MimicJpaInnerException("Id getter method not found", e);
    }
  }

  private long getIdFieldCount(Field[] fields) {
    return stream(fields)
        .filter(this::hasIdAnnotation)
        .count();
  }

  @SuppressWarnings("unchecked")
  private Class<T> getEntityType() {
    ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
    Type[] typeArguments = superclass.getActualTypeArguments();
    Type typeArgument = typeArguments[ENTITY_TYPE_INDEX];

    return (Class<T>) typeArgument;
  }

  private String getEntityName() {
    return getEntityType().getSimpleName();
  }

  private boolean hasIdAnnotation(Field field) {

    return field.getAnnotation(Id.class) != null;
  }

  private ID extractId(T entity, Field field) {
    try {
      field.setAccessible(true);
      ID id = (ID) field.get((Object) entity);
      return id;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
