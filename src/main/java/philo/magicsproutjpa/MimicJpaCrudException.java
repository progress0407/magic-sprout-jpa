package philo.magicsproutjpa;

/**
 * MimicJpaRepository에서 CRUD 중에 발생하는 예외를 정의하기 위한 클래스입니다.
 */
public class MimicJpaCrudException extends RuntimeException {

  public MimicJpaCrudException(Throwable cause) {
    super(cause);
  }
}
