package philo.magicsproutjpa.exceptions.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OneMoreEntity {

  public OneMoreEntity() {
  }

  @Id
  private Long id;

  @Id
  private Long id2;
}
