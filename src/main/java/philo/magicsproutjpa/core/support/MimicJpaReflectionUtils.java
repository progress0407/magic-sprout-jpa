package philo.magicsproutjpa.core.support;

import static java.util.Arrays.stream;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import philo.magicsproutjpa.core.exception.MimicJpaInnerException;

/**
 * 현재 프로젝트(Sprout Mimic JPA)의 도메인에 특화된 Reflection 관련 유틸리티 클래스입니다.
 */
public class MimicJpaReflectionUtils {

  private static final int ENTITY_TYPE_INDEX = 0;

  private MimicJpaReflectionUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 클래스의 인스턴스를 생성합니다.
   */
  public static <T> T newInstance(Class<T> clazz) {

    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> getEntityType(Class<?> clazz) {
    return getGenericType((Class<T>) clazz, ENTITY_TYPE_INDEX);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static Field getIdField(Field[] fields) {
    return stream(fields)
        .filter(MimicJpaReflectionUtils::hasIdAnnotation)
        .findAny()
        .get();
  }

  public static long getIdFieldCount(Field[] fields) {
    return stream(fields)
        .filter(MimicJpaReflectionUtils::hasIdAnnotation)
        .count();
  }

  public static <E> Method extractIdGetterMethod(Class<E> entity, Field[] fields) {
    String idGetterMethodName = MimicJpaReflectionUtils.extractIdGetterMethodName(fields);
    try {
      return entity.getDeclaredMethod(idGetterMethodName);
    } catch (NoSuchMethodException e) {
      throw new MimicJpaInnerException("Id getter method not found", e);
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static String extractIdGetterMethodName(Field[] fields) {
    Field idField = MimicJpaReflectionUtils.getIdField(fields);
    String idFieldName = idField.getName();
    return getIdGetterMethodName(idFieldName);
  }

  /**
   *
   * @param targetObject 실행할 객체
   * @param method 실행할 메서드
   * @return 해당 객체의 메서드의 반환 값
   * @param <O> Object type
   * @param <R> method Return type
   */
  public static <O, R> R delegateMethodInvoke(O targetObject, Method method) {
    try {
      return (R) method.invoke((Object) targetObject);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new MimicJpaInnerException("Id getter method invoke failed", e);
    }
  }

  private static String getIdGetterMethodName(String idFieldName) {
    return "get" + idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1);
  }

  private static <T> Class<T> getGenericType(Class<T> clazz, int genericIndex) {
    ParameterizedType superclass = (ParameterizedType) clazz.getGenericSuperclass();
    Type[] typeArguments = superclass.getActualTypeArguments();
    Type typeArgument = typeArguments[genericIndex];
    return (Class<T>) typeArgument;
  }

  public static <E, K> K extractId(E entity, Field field) {
    try {
      field.setAccessible(true);
      K id = (K) field.get((Object) entity);
      return id;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean hasIdAnnotation(Field field) {
    return field.getAnnotation(Id.class) != null;
  }
}
