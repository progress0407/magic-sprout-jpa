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
 * @param <E>  the type of the entity
 * @param <K> the type of the entity's identifier(Primary Key)
 */

@Slf4j
public abstract class MimicJpaRepository<E, K> {

  private static final int ENTITY_TYPE_INDEX = 0;

  private final EntityManager entityManager = createEntityManager();

  private Method idGetterMethodCache = null;

  protected MimicJpaRepository() {
    Class<E> entityType = getEntityType();
    Field[] fields = entityType.getDeclaredFields();

    assertIdFieldExists(fields);
    idGetterMethodCache = assertIdGetterExistsAndGet(entityType, fields);
  }

  /**
   * 기존에 엔티티가 존재하지 않았다면 영속화를 수행하고
   * <br>
   * 그렇지 않다면 변경을 수행합니다.
   * <br>
   * 여기서 영속화의 여부는 영속화가 되어있거나 DB에 있는 것을 기준으로 합니다.
   * <br>
   * 기존 JPA가 id가 null인지를 기준으로 하는 것과는 다르게 동작합니다.
   */
  public E save(E entity) {
    K id = getIdValue(entity);

    if (isNewEntity(id)) {
      executeInTransaction(() -> entityManager.persist(entity));
    } else {
      executeInTransaction(() -> entityManager.merge(entity));
    }

    return entity;
  }

  /**
   * 모든 엔티티를 찾아옵니다.
   */
  public List<E> findAll() {
    String selectQuery = "select e from " + getEntityName() + " e";

    return entityManager
        .createQuery(selectQuery, getEntityType())
        .getResultList();
  }

  /**
   * 키로 엔티티를 조회합니다.
   * <br>
   * 이때 엔티티는 영속화됩니다.
   */
  public E findById(K id) {
    Class<E> entityType = getEntityType();

    return entityManager.find(entityType, id);
  }

  /**
   * 모든 엔티티를 제거합니다.
   */
  public void deleteAll() {
    String deleteQuery = "delete from " + getEntityName();
    executeInTransaction(() -> entityManager.createQuery(deleteQuery).executeUpdate());
  }

  /**
   * 특정 엔티티를 키 값으로 지웁니다.
   * @param id
   */
  public void deleteById(K id) {
    E entity = entityManager.find(getEntityType(), id);
    executeInTransaction(() -> entityManager.remove(entity));
  }

  /**
   * 모든 엔티티의 갯수를 구합니다.
   * <br>
   * RDBMS 관점에서는 엔티티와 연결된 테이블의 모든 레코드를 조회합니다.
   * <br>
   */
  public long count() {
    String countQuery = "select count(e) from " + getEntityName() + " e";
    return entityManager
        .createQuery(countQuery, Long.class)
        .getSingleResult();
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

  private boolean isNewEntity(K id) {
    return id == null || entityManager.find(getEntityType(), id) == null;
  }

  private K getIdValue(E entity) {
    try {
      return (K) idGetterMethodCache.invoke(entity);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new MimicJpaInnerException(e);
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

  private Method assertIdGetterExistsAndGet(Class<E> entity, Field[] fields) {
    return extractIdGetterMethod(entity, fields);
  }

  private Method extractIdGetterMethod(Class<E> entity, Field[] fields) {
    String idGetterMethodName = extractIdGetterMethodName(fields);
    try {
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

  private long getIdFieldCount(Field[] fields) {
    return stream(fields)
        .filter(this::hasIdAnnotation)
        .count();
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

  private boolean hasIdAnnotation(Field field) {
    return field.getAnnotation(Id.class) != null;
  }

  /**
   * 성능 테스트 용으로 아직 지우지 않았다
   */
  private K extractId(E entity, Field field) {
    try {
      field.setAccessible(true);
      K id = (K) field.get((Object) entity);
      return id;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
