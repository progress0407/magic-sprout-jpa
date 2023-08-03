package philo.magicsproutjpa.business.person;

import java.util.List;
import philo.magicsproutjpa.core.MimicJpaRepository;

public class PersonRepository extends MimicJpaRepository<Person, Long> {

  public List<Person> findByName(String name) {
    return invokeQueryMethod(name);
  }

  public List<Person> findByBirthYear(int age) {
    return invokeQueryMethod(age);
  }
}
