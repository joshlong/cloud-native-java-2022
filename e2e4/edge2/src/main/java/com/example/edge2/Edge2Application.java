package com.example.edge2;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Collection;

@SpringBootApplication
public class Edge2Application {

    public static void main(String[] args) {
        SpringApplication.run(Edge2Application.class, args);
    }


    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8080/")
                .build();
    }

    @Bean
    CrmClient client(WebClient http) {
        var factory = HttpServiceProxyFactory
                .builder(new WebClientAdapter(http))
                .build();
        return factory.createClient(CrmClient.class);
    }

    @Bean
    ApplicationRunner applicationRunner(CrmClient crm) {
        return args -> crm.getCustomers().forEach(System.out::println);
    }

}


interface CrmClient {

    @GetExchange("/customers")
    Collection<Customer> getCustomers();
}

record Customer(Integer id, String name) {
}