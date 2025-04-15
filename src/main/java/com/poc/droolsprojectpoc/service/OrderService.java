package com.poc.droolsprojectpoc.service;

import com.poc.droolsprojectpoc.dto.OrderRequest;
import com.poc.droolsprojectpoc.model.Customer;
import com.poc.droolsprojectpoc.model.Order;
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
public class OrderService {

    private final KieContainer kieContainer;
    private final CustomerService customerService;
    private final Map<Long, Order> orderDB = new HashMap<>();
    private final AtomicLong orderIdCounter = new AtomicLong(1);

    @Autowired
    public OrderService(KieContainer kieContainer, CustomerService customerService) {
        this.kieContainer = kieContainer;
        this.customerService = customerService;
    }

    public Order createOrder(OrderRequest orderRequest) {
        // Check if customer exists
        Customer customer = customerService.getCustomer(orderRequest.getCustomerId());
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }

        // Create new order
        Order order = new Order();
        order.setId(orderIdCounter.getAndIncrement());
        order.setCustomerId(orderRequest.getCustomerId());
        order.setTotalAmount(orderRequest.getTotalAmount());

        // Apply rules
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(customer);
        kieSession.insert(order);
        kieSession.fireAllRules();
        kieSession.dispose();

        orderDB.put(order.getId(), order);
        return order;
    }

    public Order getOrder(Long id) {
        return orderDB.get(id);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orderDB.values());
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderDB.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }
}
