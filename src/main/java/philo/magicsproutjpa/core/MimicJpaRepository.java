package philo.magicsproutjpa.core;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.delegateMethodInvoke;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.extractIdGetterMethod;
import static philo.magicsproutjpa.core.support.MimicJpaReflectionUtils.getIdFieldCount;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import philo.magicsproutjpa.core.exception.MimicInnerException;
import philo.magicsproutjpa.core.exception.MimicJpaInitException;
import philo.magicsproutjpa.core.exception.MimicJpaInnerException;
import philo.magicsproutjpa.core.support.EntityManagerFactoryFacade;
import philo.magicsproutjpa.core.support.MimicJpaReflectionUtils;
import philo.magicsproutjpa.core.support.VoidFunction;

/**
 * SimpleJpaRepository 처럼 JPA의 기능을 모방한 클래스입니다.
 * <br>
 * 해당 클래스를 구현하는 것만으로 편리하게 JPA 기능을 사용할 수 있습니다.
 * <br>
 * 기본적인 CRUD 기능을 지원합니다.
 * <br>
 * 구체적으로 save(entity), findAll(), findByID(id), count(), delete(entity), deleteAll() 메서드를 지원합니다.
 * <br>
 * 또한 Spring Data에서 지원하는 Query Method 기능을 일부 제공합니다.
 * <br>
 * findByName 등을 작성하면 해당 이름을 가진 필드를 기준으로 검색하는 메서드를 자동으로 생성합니다.
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
   * 기존에 엔티티가 존재하지 않았다면 영속화를 하고 그렇지 않다면 변경을 수행합니다.
   * <br>
   * 여기서 영속화의 여부는 영속화가 되어있거나 DB에 있는 것을 기준으로 합니다.
   * <br>
   * 기존 Sping Data JPA가 id가 null인지를 기준으로 하는 것과는 다르게 동작합니다.
   * <br>
   * 또 Spring Data JPA과 다르게 반환 타입이 boolean입니다.
   * <br>
   * 저장을 할 경우 참이며 변경을 할 경우 거짓을 반환합니다.
   *
   * @param entity 영속 후 DB에 저장할 엔티티
   * @return 저장 여부 (true: persist 호출, false: merge 호출)
   */
  public boolean save(E entity) {
    K id = getIdValue(entity);

    if (isNewEntity(id)) {
      executeInTransaction(() -> entityManager.persist(entity));
      return true;
    }

    executeInTransaction(() -> entityManager.merge(entity));
    return false;
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
   * Spring Data JPA의 Query Method와 유사한 기능을 제공합니다.
   * <br>
   * 특정 필드 이름으로 찾기 혹은 And 조건으로 찾기 등의 기능을 지원합니다.
   * <br>
   * findBy 혹은 find로 시작하는 메서드를 작성후 카멜케이스로 필드명을 적습니다.
   * <br>
   * [예시]
   * <br>
   * findByName: name 필드를 기준으로 찾습니다.
   * <br>
   * findAge age 필드를 기준으로 찾습니다.
   * <br>
   * findByNameAndAge 에서 name과 age를 and 조건으로 찾습니다.
   * <br>
   * Spring Data JPA와 달리 invokeQueryMethod 메서드에게 위임하는 방식으로 구현해야 합니다.
   * <br>
   *
   * @param values
   * @return
   */
  protected List<E> invokeQueryMethod(Object... values) {
    String methodName = searchMethodName();
    List<String> keys = extractConditionKeys(methodName);
    Map<String, Object> keyValuePairs = mapWhereClauseKeyValues(keys, values);
    String queryString = buildQueryString(keys, keyValuePairs);
    TypedQuery<E> query = createQuery(keyValuePairs, queryString);
    return query.getResultList();
  }

  private TypedQuery<E> createQuery(Map<String, Object> keyValuePairs, String queryString) {
    TypedQuery<E> query = entityManager.createQuery(queryString, entityType());
    for (var entry : keyValuePairs.entrySet()) {
      query.setParameter(entry.getKey(), entry.getValue());
    }
    return query;
  }

  private String buildQueryString(
      List<String> keys,
      Map<String, Object> whereClauseKeyValues
  ) {
    String firstQueryPiece = String.format(
        String.format("select e from %s e where ", entityName()));

    List<String> keyStatements = keys.stream()
        .map(key -> String.format("e.%s = :%s", key, key))
        .toList();

    String andStatement = String.join(" and ", keyStatements);

    return firstQueryPiece + andStatement;
  }

  private LinkedHashMap<String, Object> mapWhereClauseKeyValues(
      List<String> conditionKeys,
      Object[] conditionValue) {
    LinkedHashMap<String, Object> keyValuePairs = new LinkedHashMap<>();
    for (int i = 0; i < conditionKeys.size(); i++) {
      keyValuePairs.put(conditionKeys.get(i), conditionValue[i]);
    }
    return keyValuePairs;
  }

  private String searchMethodName() {
    return stream(currentThread().getStackTrace())
        .filter(this::equalsClassName)
        .map(StackTraceElement::getMethodName)
        .findAny()
        .orElseThrow(MimicInnerException::new);
  }

  private boolean equalsClassName(StackTraceElement stackTraceElement) {
    return stackTraceElement.getClassName().equals(domainRepositoryClassCache.getName());
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

  /**
   * 문자열에서 유의미한 필드명을 추출합니다.
   * <br>
   * 예를들어 "findByNameAndAge"에서 ["name", "age"]를 추출합니다.
   *
   * @param methodName 메서드 이름
   * @return 필드 이름들
   */
  private static List<String> extractConditionKeys(String methodName) {
    String whereClause = methodName.replaceFirst("findBy|find", "");
    return stream(whereClause.split("And"))
        .map(word -> word.substring(0, 1).toLowerCase() + word.substring(1))
        .toList();
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

  /**
   * 엔티티의 ID값을 불러옵니다
   * @param entity
   * @return ID값
   */
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
