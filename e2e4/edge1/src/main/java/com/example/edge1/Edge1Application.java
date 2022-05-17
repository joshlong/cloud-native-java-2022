package com.example.edge1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Edge1Application {

    @Bean
    RouteLocator myGateway(RouteLocatorBuilder rlb) {
        return rlb
                .routes()
                .route(rs -> rs
                        .path("/proxy")
                        .filters(fs -> fs
                                .setPath("/customers")
                                .addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        )
                        .uri("http://localhost:8080/")
                )
                .build();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }


    public static void main(String[] args) {
        SpringApplication.run(Edge1Application.class, args);
    }

}

@Controller
class CrmGraphqlController {

    private final WebClient http;

    CrmGraphqlController(WebClient http) {
        this.http = http;
    }

    //    @SchemaMapping(typeName = "Query", field = "customers")
    @QueryMapping
    Flux<Customer> customers() {
        return this.http.get()
                .uri("http://localhost:8080/customers")
                .retrieve()
                .bodyToFlux(Customer.class);
    }

    @BatchMapping
    Map<Customer, Profile> profile(List<Customer> customers) {
        var map = new HashMap<Customer, Profile>();
        for (var c : customers)
            map.put(c, new Profile(c.id()));
        return map;
    }


  /*  @SchemaMapping(typeName = "Customer")
    Mono<Profile> profile(Customer customer) throws Exception {
        System.out.println("getting profile for customer " + customer.id() + "@ " + new Date());
        return Mono.just(new Profile(customer.id()))
                .delayElement(Duration.ofSeconds(1));
    }*/
}

record Profile(Integer id) {
}

record Customer(Integer id, String name) {
}