package com.poc.droolsprojectpoc.service;

import com.poc.droolsprojectpoc.model.Customer;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {

    private final KieContainer kieContainer;
    private final Map<Long, Customer> customerDB = new HashMap<>();
    private final AtomicLong customerIdCounter = new AtomicLong(1);

    @Autowired
    public CustomerService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;

        // Add some sample customers
        addCustomer(new Customer(null, "John Doe", 25, "john@example.com"));
        addCustomer(new Customer(null, "Jane Smith", 45, "jane@example.com"));
        addCustomer(new Customer(null, "Robert Brown", 65, "robert@example.com"));
    }

    public Customer addCustomer(Customer customer) {
        Long id = customerIdCounter.getAndIncrement();
        customer.setId(id);

        // Apply rules to determine customer category
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(customer);
        kieSession.fireAllRules();
        kieSession.dispose();

        customerDB.put(id, customer);
        return customer;
    }

    public Customer getCustomer(Long id) {
        return customerDB.get(id);
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customerDB.values());
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        if (!customerDB.containsKey(id)) {
            return null;
        }

        updatedCustomer.setId(id);

        // Reapply rules
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(updatedCustomer);
        kieSession.fireAllRules();
        kieSession.dispose();

        customerDB.put(id, updatedCustomer);
        return updatedCustomer;
    }

    public boolean deleteCustomer(Long id) {
        return customerDB.remove(id) != null;
    }
}
