package philo.magicsproutjpa.performance;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import philo.magicsproutjpa.core.support.VoidFunction;
import philo.magicsproutjpa.domain.Item;
import philo.magicsproutjpa.domain.ItemRepository;

/**
 * 성능에 관련된 테스트 입니다
 * 
 * <br>
 * 예상과 다르게 캐싱여부가 속도에 크게 무관한 것으로 밝혀졌음
 */
class PerformanceTest {

  static final int TRY_COUNT = 1000;
  static final String LOG_FORMAT_STRING = "\u001B[32m[%s] elapsedMilliSecond = %s\u001B[0m";

  final List<String> logs = new LinkedList<>();

  @Disabled
  @DisplayName("일반적으로 메소드 호출을 캐싱을 한 것이 캐싱을 하지 않은 것보다 속도가 빠르다")
  @Test
  void performance() {
    // given
    var cacheRepository = new ItemRepository();
    var nonCacheRepository = new NonCacheItemRepository();

    // jvm warm up
    warmUp(() -> nonCacheRepository.save(new Item("something")));
    warmUp(() -> cacheRepository.save(new Item("something")));

    // when
    long cacheElapsedTime = measureTime("CACHE", () -> cacheRepository.save(new Item("something")));
    long nonCacheElapsedTime = measureTime("NON CACHE", () -> nonCacheRepository.save(new Item("something")));

    // then
    printEachLogs();
    assertThat(cacheElapsedTime).isLessThan(nonCacheElapsedTime);
  }

  /**
   * 반복 시행횟수 만큼 시행한 함수의 실행 시간을 측정하고 로그 리스트에 적재한다
   *
   * @param topicName 측정 대상에 대한 주제
   * @param function 측정할 로직
   * @return 측정한 시간
   */
  private long measureTime(String topicName, VoidFunction function) {
    long start = currentTimeMillis();
    repetitiveRun(function);
    long elapsedMilliSecond = currentTimeMillis() - start;
    addLog(topicName, elapsedMilliSecond);
    return elapsedMilliSecond;
  }

  private void warmUp(VoidFunction function) {
    repetitiveRun(function);
  }

  private static void repetitiveRun(VoidFunction function) {
    for (int i = 0; i < TRY_COUNT; i++) {
      function.execute();
    }
  }

  private void addLog(String topicName, long elapsedMilliSecond) {
    String logString = String.format(LOG_FORMAT_STRING, topicName, elapsedMilliSecond);
    logs.add(logString);
  }

  private void printEachLogs() {
    logs.forEach(out::println);
  }
}
