package philo.magicsproutjpa;

import static java.util.Arrays.stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import jakarta.persistence.Persistence;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SimpleJpaRepository 처럼 JPA의 기능을 모방한 클래스입니다.
 *
 * @param <T>  the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 */

@RequiredArgsConstructor
@Slf4j
public abstract class MimicJpaRepository<T, ID> {

  private static final int ENTITY_TYPE_INDEX = 0;

  private final EntityManager entityManager = createEntityManager();

  T save(T entity) {

    ID id = getId(entity);

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

    executeInTransaction(() ->
        entityManager
            .createQuery(deleteQuery)
            .executeUpdate()
    );
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

  // todo getId() 메서드를 통해 id를 가져오는 방법을 개선해야 합니다.
  // 현재는 @Id 어노테이션이 붙은 필드를 찾아서 reflection을 통해 값을 가져오는 방식입니다.
  // 이 방식은 캡슐화를 깨는 방식이므로, 다른 방법을 찾아야 합니다.
  private ID getId(T entity) {

    Field[] fields = getEntityType().getDeclaredFields();

    return stream(fields)
        .filter(this::hasIdAnnotation)
        .map(field -> extractId(entity, field))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
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
