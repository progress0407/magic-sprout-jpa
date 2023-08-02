package philo.magicsproutjpa.core.support;

/**
 * 이 인터페이스는 {@link VoidFunction#execute()} 메서드 하나만 가지고 있습니다. (함수형 인터페이스)
 * <br>
 * `void -> void` 타입의 함수형 인터페이스가 필요해서 만들었습니다.
 * <br>
 * Runnable 인터페이스는 동일한 타입의 함수형 인터페이스이지만,
 * <br>
 * 관례상 쓰레드에서 사용할 목적으로 만들었으므로 별도로 이 인터페이스를 만들었습니다.
 */
@FunctionalInterface
public interface VoidFunction {

  void execute();
}
