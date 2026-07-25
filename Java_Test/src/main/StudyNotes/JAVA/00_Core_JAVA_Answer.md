1. What are the four pillars of OOP?
2. Difference between abstraction and encapsulation?

---

3. Difference between abstraction and interface?
| Feature              | Abstract Class      | Interface                          |
| -------------------- | ------------------- | ---------------------------------- |
| Constructor          | Yes                 | No                                 |
| Instance Variables   | Yes                 | Only public static final constants |
| Multiple Inheritance | No                  | Yes                                |
| Methods              | Abstract + Concrete | Abstract + Default + Static        |
| Access Modifiers     | Any                 | Methods are public by default      |
| State                | Can maintain state  | Cannot maintain instance state     |

Abstraction is the OOP principle of exposing only essential features while hiding implementation details. In Java, abstraction can be achieved using both abstract classes and interfaces. 
An interface defines a contract that implementing classes must follow, making it one of the primary tools used to achieve abstraction. Since Java 8, interfaces can also provide default and static method implementations, but their primary purpose remains defining behavior rather than maintaining state.

---

4. Difference between inheritance and composition?
| Feature                    | Inheritance         | Composition        |
| -------------------------- | ------------------- | ------------------ |
| Relationship               | IS-A                | HAS-A              |
| Coupling                   | Tight               | Loose              |
| Reusability                | Through inheritance | Through delegation |
| Flexibility                | Less                | More               |
| Runtime change             | Difficult           | Easy               |
| Encapsulation              | Can be weakened     | Better maintained  |
| Preferred in modern design | Usually No          | Usually Yes        |

Inheritance models an IS-A relationship and enables code reuse through subclassing, but it creates tight coupling between parent and child classes. Composition models a HAS-A relationship by combining objects and delegating behavior. Composition is generally preferred because it provides better flexibility, lower coupling, improved encapsulation, and allows behavior to change without modifying class hierarchies. That's why modern object-oriented design follows the principle "Favor Composition Over Inheritance."

```java
// Dog IS-A Animal

class Animal {
    void eat() {
        System.out.println("Eating");
    }
}

class Dog extends Animal {
    void bark() {
        System.out.println("Barking");
    }
}

Dog dog = new Dog();
dog.eat();
dog.bark();
```

```java
// Car HAS-A Engine

class Engine {
    void start() {
        System.out.println("Engine Started");
    }
}

class Car {
    private Engine engine;
    Car() {
        this.engine = new Engine();
    }
    void startCar() {
        engine.start();
    }
}

Car car = new Car();
car.startCar();
```

---

5. 
6. 
