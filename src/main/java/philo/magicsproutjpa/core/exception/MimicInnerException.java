package philo.magicsproutjpa.core.exception;

import lombok.NoArgsConstructor;

/**
 * MimicJpaRepository에서 CRUD 중에 발생하는 예외를 정의하기 위한 클래스입니다.
 */
@NoArgsConstructor
public class MimicInnerException extends MimicJpaInnerException {

  public MimicInnerException(Throwable cause) {
    super(cause);
  }
}
