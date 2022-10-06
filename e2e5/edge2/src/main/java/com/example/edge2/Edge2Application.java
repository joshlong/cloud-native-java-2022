package com.example.edge2;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.DecoratingProxy;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RegisterReflectionForBinding(Customer.class)
@ImportRuntimeHints(Edge2Application.Hints.class)
public class Edge2Application {

    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints
                    .proxies()
                    .registerJdkProxy(
                            com.example.edge2.CrmClient.class, org.springframework.aop.SpringProxy.class,
                            org.springframework.aop.framework.Advised.class, DecoratingProxy.class
                    );
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Edge2Application.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> httpClientRunner(CrmClient crmClient) {
        return event -> crmClient.getCustomers().subscribe(System.out::println);
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(WebClient.Builder http) {
        return WebClientAdapter.createHttpServiceProxyFactory(http.baseUrl("http://localhost:8080/"));
    }

    @Bean
    CrmClient crmClient(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(CrmClient.class);
    }
}

interface CrmClient {

    @GetExchange(url = "/customers")
    Flux<Customer> getCustomers();
}

record Customer(Integer id, String name) {
}