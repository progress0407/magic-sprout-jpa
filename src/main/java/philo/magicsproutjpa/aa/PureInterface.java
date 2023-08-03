package philo.magicsproutjpa.aa;

import java.util.List;

public interface PureInterface<E, K> {

  List<E> findAll();

  E findById(K id);

}
