package com.example.customers;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
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
    HealthIndicator healthIndicator() {
        return () -> Health.status("I <3 Production!").build();
    }
}

@Controller
@ResponseBody
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
class HealthController {

    private final ApplicationContext context;

    HealthController(ApplicationContext context) {
        this.context = context;
        System.out.println("hello, K8s!");
    }

    @GetMapping("/down")
    void down() {
        AvailabilityChangeEvent.publish(this.context,
                LivenessState.BROKEN);
    }
}

@Controller
@ResponseBody
class CustomerRestController {

    private final CustomerRepository repository;

    CustomerRestController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    Flux<Customer> customers() {
        return this.repository.findAll();
    }
}

@Component
class Initializer implements ApplicationRunner {

    private final CustomerRepository repository;

    Initializer(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Flux.just("Josh", "Jürgen", "Stéphane", "Olga",
                        "Violetta", "Yuxin", "Madhura", "Dr. Syer")
                .map(name -> new Customer(null, name))
                .flatMap(this.repository::save)
                .subscribe(System.out::println);
    }
}


record Customer(@Id Integer id, String name) {
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}