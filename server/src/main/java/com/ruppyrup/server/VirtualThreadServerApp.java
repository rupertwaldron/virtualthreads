package com.ruppyrup.server;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.concurrent.Executors;

@SpringBootApplication
public class VirtualThreadServerApp {

    public static void main(String[] args) {
        SpringApplication.run(VirtualThreadServerApp.class);
    }

    @Bean
    ApplicationRunner applicationRunner(CustomerService service) {
        return args -> service.all().forEach(System.out::println);
    }

//    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
//    public AsyncTaskExecutor asyncTaskExecutor() {
//        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
//    }
//
//    @Bean
//    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
//        return protocolHandler -> {
//            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
//        };
//    }
}

@RestController
class CustomerHttpController {

    private final CustomerService customerService;

    CustomerHttpController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customers")
    private Collection<Customer> all() {
        return customerService.all();
    }

    @GetMapping("/customers/{name}")
    private Collection<Customer> getBy(@PathVariable String name) {
        return customerService.byName(name);
    }
}

@Service
class CustomerService {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Customer> customerRowMapper = (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name"));

    CustomerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Collection<Customer> byName(String name) {
        return jdbcTemplate.query("select * from customer where name = ?", customerRowMapper, name);
    }

    Collection<Customer> all() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return jdbcTemplate.query("select * from customer", customerRowMapper);
    }
}

record Customer(Integer Id, String name){}
