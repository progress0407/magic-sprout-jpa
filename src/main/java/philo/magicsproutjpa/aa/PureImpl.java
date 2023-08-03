package philo.magicsproutjpa.aa;

import java.util.List;

public class PureImpl<E, K> implements PureInterface<E, K> {


  @Override
  public List<E> findAll() {
    System.out.println("PureImpl.findAll");
    return null;
  }

  @Override
  public E findById(K id) {
    System.out.println("PureImpl.findById");
    return null;
  }
}
