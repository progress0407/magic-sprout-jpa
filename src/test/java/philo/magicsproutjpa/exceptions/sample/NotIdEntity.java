package philo.magicsproutjpa.exceptions.sample;

import jakarta.persistence.Entity;

@Entity
public class NotIdEntity {

  public NotIdEntity() {
  }

  private Long id;
}
