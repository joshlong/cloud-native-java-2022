package com.example.edge1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class Edge1Application {

    public static void main(String[] args) {
        SpringApplication.run(Edge1Application.class, args);
    }


    @Bean
    RouteLocator gateway(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder
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

}

@Controller
class CrmGraphqlController {

    private final WebClient http;

    CrmGraphqlController(WebClient http) {
        this.http = http;
    }

    @SchemaMapping(typeName = "Customer", field = "profile")
    Profile profile(Customer customer) {
        return new Profile(customer.id());
    }

    @QueryMapping
//    @SchemaMapping(typeName = "Query", field = "customers")
    Flux<Customer> customers() {
        return this.http.get()
                .uri("http://localhost:8080/customers")
                .retrieve()
                .bodyToFlux(Customer.class);
    }

}

record Profile(Integer id) {
}

record Customer(Integer id, String name) {
}