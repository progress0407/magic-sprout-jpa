package philo.magicsproutjpa;

/**
 * MimicJpaRepository에서 발생하는 예외를 정의하기 위한 클래스입니다.
 */
public class MimicJpaInnerException extends RuntimeException {

  public MimicJpaInnerException(String message) {
    super(message);
  }

  public MimicJpaInnerException(Throwable cause) {
    super(cause);
  }

  public MimicJpaInnerException(String message, Throwable cause) {
    super(message, cause);
  }
}
