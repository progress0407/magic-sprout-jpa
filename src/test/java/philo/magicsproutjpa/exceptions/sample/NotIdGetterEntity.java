package philo.magicsproutjpa.exceptions.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class NotIdGetterEntity {

  public NotIdGetterEntity() {
  }

  @Id
  private Long id;
}
