package com.example.herokupipeexample;

import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicLong;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
public class CustomerController extends GraphiteMetricsConfig
{
    private CustomerRepository customerRepository;
    private MetricRegistry registry = registry();

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Counter numberOfCustomers = registry.counter("Customer counter");
    private final Timer timer = registry.timer("Get list timer");

    public CustomerController(CustomerRepository customerRepository)
    {
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void initialize()
    {
        GraphiteReporter reporter = getReporter(registry);
        reporter.report();
    }

    @RequestMapping("/")
    public String welcome()
    {
        registry.meter("welcome message").mark();
        return "Welcome to this small REST service. It will accept a GET on /list with a request parameter lastName, and a POST to / with a JSON payload with firstName and lastName as values.";
    }

    @RequestMapping("/list")
    public List<Customer> find(@RequestParam(value="lastName") String lastName)
    {
        Timer.Context context = timer.time();
        logger.info("Finding customers with last name => " + lastName);
        List<Customer> list = customerRepository.findByLastName(lastName);
        context.stop();
        return list;
    }

    @PostMapping("/")
    Customer newCustomer(@RequestBody Customer customer)
    {
        logger.info("Newly created customer =>" + customer);
        numberOfCustomers.inc();
        return customerRepository.save(customer);
    }

//    This is not at good practice, only testing purpose
    @DeleteMapping("/")
    public void deleteAll()
    {
        logger.info("Deleting everything");
        numberOfCustomers.dec(numberOfCustomers.getCount());
        customerRepository.deleteAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") String pathId)
    {
        Long id;

        try {
            id = Long.valueOf(pathId);
        }
        catch (Exception e) {
            return ResponseEntity.status(400).build();
        }

        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(404).build();
        }

        customerRepository.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
