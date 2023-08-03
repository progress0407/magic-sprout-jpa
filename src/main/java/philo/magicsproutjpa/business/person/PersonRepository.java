package philo.magicsproutjpa.business.person;

import java.util.List;
import philo.magicsproutjpa.core.MimicJpaRepository;

public class PersonRepository extends MimicJpaRepository<Person, Long> {

  public List<Person> findByName(String name) {
    return invokeQueryMethod(name);
  }


  public List<Person> findBirthYear(int age) {
    return invokeQueryMethod(age);
  }

  public List<Person> findByNameAndBirthYear(String name, int age) {
    return invokeQueryMethod(name, age);
  }
}
