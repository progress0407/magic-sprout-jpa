# sprout-mimic-jpa :  새싹 모방 JPA

## [이 프로젝트에 대한 설명]

Spring Data JPA의 인터페이스와 같은 역할을 하는 기능을 간략하게 만든 프로젝트이다

- MimicJpaRepository를 상속하면 save, findAll 등 **CRUD 메서드**를 구현하지 않더라도 **자동으로 제공**되어 편리하게 사용할 수 있다
- **Query Method** 기능을 지원한다
  - findByName 등을 작성하면 name에 해당하는 필드를 기준으로 조회한다

### [ CRUD Auto Generate ]

**[ Implementation ]**

```java
public class ItemRepository extends MimicJpaRepository<Item, Long> {
}
```

**[ Usage ]**

```java
// Create or Update
itemRepository.save(new Item("black jean"));
        
// Read
Item foundItem = itemRepository.findById(1);
List<Item> allItems = itemRepository.findAll();
long recordCount = itemRepository.count();

// Delete
itemRepository.deleteById(1L);
itemRepository.deleteAll();
```

### [ Query Method ]

**[ Implementation ]**

```java
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
```

**[ Usage ]**

```java
List<Person> foundPeople = personRepository.findByName("IU");
List<Person> foundPeople = personRepository.findBirthYear(87);
List<Person> foundPeople = personRepository.findByNameAndBirthYear("IU", 93);
```

## [이 프로젝트를 만든 이유]

1년 전 우아한테크코스 방학 때 제네릭과 JPA와 친해지려는 목적으로

Spring Data JPA의 인터페이스와 같은 역할을 하는 기능을 간략하게 만든 적이 있었다

Git Repository를 병합하고 옮기는 과정에서 일부 유실된 코드도 있고 기능적으로 부실한 부분, Smell Code등이 보여서

더 좋은 프로젝트로 만들고자 작성했다

## [고민한 점]

### [클린 코드] public 메서드는 가급적 Lambda-Stream으로 작성할 것

이 부분이 쉽지 않았다  

GPT 4.0 + 영어 질문으로 해결한 케이스이다

### [OOP] 캡슐화를 생각함

Reflection + setAccessible(true)를 사용하면 쉽게 접근할 수 있지만 캡슐화를 위반하므로

getter메서드가 있는지를 확인

### [기술 스택] 스프링에 의존하지 않고 작성

Spring Data의 기능을 구현하고자 하는 목적으로 만든 것이므로  
Spring의 기능을 사용하지 않고 순수 Hibernate의 기능만 사용했다

![image](https://github.com/progress0407/progress0407/assets/66164361/77a5f3c0-d593-415e-abf2-5f0544e9c507)
<img src="https://github.com/progress0407/progress0407/assets/66164361/e592d2ff-0b4e-4b0a-b250-210291a230c8" width="600" alt="...">

(Spring의 기능을 사용하지 않았다)

### [성능] 성능을 고려하려고 노력 (실패)

PK의 Getter가 있는지 확인하는 과정에서 **리플렉션 작업**을 많이 수행하기 때문에 많은 오버헤드가 있을 것으로 생각됨   
따라서 이것을 캐싱해서 사용하는 쪽으로 변경  

- 그러나 예상과는 다르게 **성능**의 **차이**가 **유의미**하게 **발생**하지 **않았다**

[시도한 점]
혹시나 성능 측정 과정에서 잘못된 것이 있을 수 있으므로 몇 가지 시도를 해봤다
- 시행횟수 증가
- JVM 웜업을 고려해 반복 실행 과정을 미리 심어놓음

그러나 역시... 결과는 마찬가지였다
