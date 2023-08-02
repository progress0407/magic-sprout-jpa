package philo.magicsproutjpa.core.exception;

/**
 * MimicJpaRepository에서 초기화할 때 발생하는 예외를 정의하기 위한 클래스입니다.
 */
public class MimicJpaInitException extends MimicJpaInnerException {

  public MimicJpaInitException(String message) {
    super(message);
  }

  public MimicJpaInitException(Throwable cause) {
    super(cause);
  }

  public MimicJpaInitException(String message, Throwable cause) {
    super(message, cause);
  }
}
