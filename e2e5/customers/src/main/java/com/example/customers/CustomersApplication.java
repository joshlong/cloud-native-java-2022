package com.example.customers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class CustomersApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomersApplication.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> dataListener(CustomerRepository repository) {
        return event -> {
            var names = Flux
                    .just("Yuxin", "Olga", "Stéphane", "Jürgen", "Dr. Syer",
                            "Josh", "Violetta", "Olivier")
                    .map(name -> new Customer(null, name))
                    .flatMap(repository::save);
            names.subscribe(System.out::println);
        };
    }

}

@Controller
@ResponseBody
class HealthHttpController {

    private final ApplicationContext ac;

    HealthHttpController(ApplicationContext ac) {
        this.ac = ac;
    }

    @GetMapping("/down")
    void down() {
        AvailabilityChangeEvent.publish(this.ac, LivenessState.BROKEN);
    }


}

@Controller
@ResponseBody
class CustomerHttpController {

    private final CustomerRepository repository;

    CustomerHttpController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    Flux<Customer> get() {
        return this.repository.findAll();
    }
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

record Customer(@Id Integer id, String name) {
}