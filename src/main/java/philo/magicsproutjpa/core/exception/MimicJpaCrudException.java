package philo.magicsproutjpa.core.exception;

/**
 * MimicJpaRepository에서 CRUD 중에 발생하는 예외를 정의하기 위한 클래스입니다.
 */
public class MimicJpaCrudException extends MimicJpaInnerException {

  public MimicJpaCrudException(Throwable cause) {
    super(cause);
  }
}
