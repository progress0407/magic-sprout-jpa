package philo.magicsproutjpa.core;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.delegateMethodInvoke;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.extractIdGetterMethod;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.getIdFieldCount;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import philo.magicsproutjpa.core.exception.MimicInnerException;
import philo.magicsproutjpa.core.exception.MimicJpaInitException;
import philo.magicsproutjpa.core.exception.MimicJpaInnerException;
import philo.magicsproutjpa.core.support.EntityManagerFactoryFacade;
import philo.magicsproutjpa.core.support.MimicJpaReflectionUtils;
import philo.magicsproutjpa.core.support.VoidFunction;

/**
 * SimpleJpaRepository 처럼 JPA의 기능을 모방한 클래스입니다.
 *
 * @param <E> the type of the entity
 * @param <K> the type of the entity's identifier(Primary Key)
 */

@Slf4j
public abstract class MimicJpaRepository<E, K> {

  private static final String SELECT_QUERY_STRING = "select e from %s e";
  private static final String DELETE_QUERY_STRING = "delete from %s";
  private static final String COUNT_QUERY_STRING = "select count(e) from %s e";

  private final EntityManager entityManager = EntityManagerFactoryFacade.createEntityManager();

  private Class<? extends MimicJpaRepository<E, K>> domainRepositoryClassCache;
  private Method idGetterMethodCache = null; // 도메인 리포지토리 생성시 자동 초기화

  protected MimicJpaRepository() {
    Class<E> entityType = entityType();
    Field[] fields = entityType.getDeclaredFields();

    assertIdFieldExist(fields);
    idGetterMethodCache = assertIdGetterExistAndGet(entityType, fields);
    domainRepositoryClassCache = getDomainRepository();
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
    String selectQuery = String.format(SELECT_QUERY_STRING, entityName());

    return entityManager
        .createQuery(selectQuery, entityType())
        .getResultList();
  }

  /**
   * 키로 엔티티를 조회합니다.
   * <br>
   * 이때 엔티티는 영속화됩니다.
   */
  public E findById(K id) {
    Class<E> entityType = entityType();
    return entityManager.find(entityType, id);
  }

  /**
   * 모든 엔티티를 제거합니다.
   */
  public void deleteAll() {
    String deleteQuery = String.format(DELETE_QUERY_STRING, entityName());
    executeInTransaction(() -> entityManager.createQuery(deleteQuery).executeUpdate());
  }

  /**
   * 특정 엔티티를 키 값으로 지웁니다.
   */
  public void deleteById(K id) {
    E entity = entityManager.find(entityType(), id);
    executeInTransaction(() -> entityManager.remove(entity));
  }

  /**
   * 모든 엔티티의 갯수를 구합니다.
   * <br>
   * RDBMS 관점에서는 엔티티와 연결된 테이블의 모든 레코드를 조회합니다.
   */
  public long count() {
    String countQuery = String.format(COUNT_QUERY_STRING, entityName());
    return entityManager
        .createQuery(countQuery, Long.class)
        .getSingleResult();
  }

  /**
   * Query Method
   */
  protected List<E> invokeQueryMethod(Object conditionValue) {
    StackTraceElement[] stackTraceElements = currentThread().getStackTrace();
    StackTraceElement previousMethod = stackTraceElements[2];
    // 위는 없앨 예정
    String methodName =
        stream(currentThread().getStackTrace())
            .filter(stackTraceElement -> stackTraceElement.getClassName()
                .equals(domainRepositoryClassCache.getName()))
            .map(StackTraceElement::getMethodName)
            .findAny()
            .orElseThrow(MimicInnerException::new);
    String conditionKey = extractConditionKey(methodName);
    String selectQueryFormat = "select e from %s e where e.%s = :%s";
    String queryString = String.format(selectQueryFormat, entityName(), conditionKey, conditionKey);
    List<E> resultList = entityManager.createQuery(queryString, entityType())
        .setParameter(conditionKey, conditionValue)
        .getResultList();

    return resultList;
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
      throw new MimicInnerException(e);
    }
  }

  private static String extractConditionKey(String methodName) {
    String whereClause = methodName.replaceFirst("findBy|find", "");
    String firstLetterLowerCase = whereClause.substring(0, 1).toLowerCase();
    String extraLetters = whereClause.substring(1);
    return firstLetterLowerCase + extraLetters;
  }

  private void assertIdFieldExist(Field[] fields) {
    long idFieldCount = getIdFieldCount(fields);

    if (idFieldCount == 0) {
      throw new MimicJpaInitException("Id field not found");

    } else if (idFieldCount >= 2) {
      throw new MimicJpaInitException("Multiple Id fields found");
    }
  }

  /**
   * ID Getter가 존재하는지 여부를 조회 합니다.
   * <br>
   * 검증 성공시 해당 Method를 반환합니다.
   *
   * @param entity 엔티티 타입 클래스
   * @param fields 위 클래스에 속하는 filed들
   * @return ID Getter Method
   */
  private Method assertIdGetterExistAndGet(Class<E> entity, Field[] fields) {
    try {
      return extractIdGetterMethod(entity, fields);
    } catch (MimicJpaInnerException e) { // 예외 전환
      throw new MimicJpaInitException("Id getter method should exist", e);
    }
  }

  private boolean isNewEntity(K id) {
    return id == null
        || entityManager.find(entityType(), id) == null;
  }

  private K getIdValue(E entity) {
    return delegateMethodInvoke(entity, idGetterMethodCache);
  }

  private Class<E> entityType() {
    return MimicJpaReflectionUtils.getEntityType(this.getClass()); // delegate
  }

  private String entityName() {
    return entityType().getSimpleName();
  }

  private Class<? extends MimicJpaRepository<E, K>> getDomainRepository() {
    return (Class<? extends MimicJpaRepository<E, K>>) this.getClass();
  }
}
