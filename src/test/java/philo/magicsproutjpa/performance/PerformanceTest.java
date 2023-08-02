package philo.magicsproutjpa.performance;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import philo.magicsproutjpa.core.support.VoidFunction;
import philo.magicsproutjpa.domain.Item;
import philo.magicsproutjpa.domain.ItemRepository;

/**
 * 성능에 관련된 테스트 입니다
 */
class PerformanceTest {

  private static final int TRY_COUNT = 10;

  @DisplayName("일반적으로 메소드 호출을 캐싱을 한 것이 캐싱을 하지 않은 것보다 속도가 빠르다")
  @Test
  void performance() {
    // given
    var cacheRepository = new ItemRepository();
    var nonCacheRepository = new NonCacheItemRepository();

    // when
    long cacheElapsedTime = measureTime(() -> cacheRepository.save(new Item("something")));
    long nonCacheElapsedTime = measureTime(() -> nonCacheRepository.save(new Item("something")));

    // then
    assertThat(cacheElapsedTime).isLessThan(nonCacheElapsedTime);
  }

  /**
   * 함수의 실행 시간을 측정
   *
   * @param function 실행할 함수
   * @return `TRY_COUNT`만큼 시행한 함수의 실행 시간
   */
  private long measureTime(VoidFunction function) {
    long start = currentTimeMillis();
    for (int i = 0; i < TRY_COUNT; i++) {
      function.execute();
    }
    long end = currentTimeMillis();
    long elapsedMilliSecond = end - start;
    out.println("elapsedMilliSecond = " + elapsedMilliSecond);
    return elapsedMilliSecond;
  }
}
