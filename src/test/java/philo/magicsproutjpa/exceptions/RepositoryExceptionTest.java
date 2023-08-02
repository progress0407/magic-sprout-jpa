package philo.magicsproutjpa.exceptions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import philo.magicsproutjpa.core.exception.MimicJpaInitException;
import philo.magicsproutjpa.exceptions.sample.NotIdEntityRepository;
import philo.magicsproutjpa.exceptions.sample.NotIdGetterRepository;
import philo.magicsproutjpa.exceptions.sample.OneMoreEntityRepository;

class RepositoryExceptionTest {


  @DisplayName("엔티티의 ID 필드가 존재해야 한다")
  @Test
  void id_not_exist() {

    assertThatThrownBy(NotIdEntityRepository::new)
        .isInstanceOf(MimicJpaInitException.class)
        .hasMessage("Id field not found");
  }


  @DisplayName("엔티티의 ID 필드가 2개 이상 존재할 수 없다")
  @Test
  void id_should_not_one_more() {

    assertThatThrownBy(OneMoreEntityRepository::new)
        .isInstanceOf(MimicJpaInitException.class)
        .hasMessage("Multiple Id fields found");
  }


  @DisplayName("엔티티의 ID 필드가 존재하더라도 Getter가 없으면 안됀다")
  @Test
  void id_getter_not_exist() {

    assertThatThrownBy(NotIdGetterRepository::new)
        .isInstanceOf(MimicJpaInitException.class)
        .hasMessage("Id getter method should exist");
  }
}
