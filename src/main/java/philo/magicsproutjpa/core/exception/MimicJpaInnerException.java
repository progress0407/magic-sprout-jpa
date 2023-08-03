package philo.magicsproutjpa.core.exception;

import lombok.NoArgsConstructor;

/**
 * MimicJpaRepository에서 발생하는 예외를 정의하기 위한 클래스입니다.
 */
@NoArgsConstructor
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
