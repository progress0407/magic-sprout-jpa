package philo.magicsproutjpa.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import philo.magicsproutjpa.business.person.Person;
import philo.magicsproutjpa.business.person.PersonRepository;

class QueryMethodRepositoryTest {

  static PersonRepository personRepository = new PersonRepository();

  @AfterEach
  void tearDown() {
    personRepository.deleteAll();
  }

  @DisplayName("[Query Method] name을 기준으로 찾을 수 있다")
  @Test
  void queryMethodByName() {
    // given
    Person person1 = new Person("IU", 93);
    Person person2 = new Person("Jay Park", 87);
    personRepository.save(person1);
    personRepository.save(person2);

    // when
    List<Person> foundPeople = personRepository.findByName("IU");

    // then
    Person foundFirstPerson = foundPeople.get(0);

    assertAll(
        () -> assertThat(foundPeople).hasSize(1),
        () -> assertThat(foundFirstPerson.getId()).isEqualTo(person1.getId())
    );
  }

  @DisplayName("[Query Method] age을 기준으로 찾을 수 있다")
  @Test
  void queryMethodByAge() {
    // given
    Person person1 = new Person("IU", 93);
    Person person2 = new Person("Jay Park", 87);
    personRepository.save(person1);
    personRepository.save(person2);

    // when
    List<Person> foundPeople = personRepository.findByBirthYear(87);

    // then
    Person foundFirstPerson = foundPeople.get(0);

    assertAll(
        () -> assertThat(foundPeople).hasSize(1),
        () -> assertThat(foundFirstPerson.getId()).isEqualTo(person2.getId())
    );
  }
}