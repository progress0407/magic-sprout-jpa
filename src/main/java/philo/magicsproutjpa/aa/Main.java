package philo.magicsproutjpa.aa;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Main {

  private final ARepo aRepo;

  public static void main(String[] args) {
    Main main = new Main(new ARepoImpl());
    main.doSomething();
  }

  public void doSomething() {
    aRepo.findAll();
  }

}
