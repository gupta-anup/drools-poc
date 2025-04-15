package com.poc.droolsprojectpoc;

import com.poc.droolsprojectpoc.model.Customer;
import com.poc.droolsprojectpoc.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DroolsRuleTest {

    private KieContainer kieContainer;

    @BeforeEach
    public void setup() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/customer-rules.drl"));
        kieFileSystem.write(ResourceFactory.newClassPathResource("rules/order-rules.drl"));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Test
    public void testCustomerCategorization() {
        KieSession kieSession = kieContainer.newKieSession();

        Customer youngCustomer = new Customer(1L, "Alice", 25, "alice@example.com");
        Customer adultCustomer = new Customer(2L, "Bob", 45, "bob@example.com");
        Customer seniorCustomer = new Customer(3L, "Charlie", 65, "charlie@example.com");
        Customer premiumCustomer = new Customer(4L, "David", 35, "david@premium.com");

        kieSession.insert(youngCustomer);
        kieSession.insert(adultCustomer);
        kieSession.insert(seniorCustomer);
        kieSession.insert(premiumCustomer);

        kieSession.fireAllRules();
        kieSession.dispose();

        assertEquals("Young", youngCustomer.getCategory());
        assertEquals(5, youngCustomer.getDiscount());

        assertEquals("Adult", adultCustomer.getCategory());
        assertEquals(0, adultCustomer.getDiscount());

        assertEquals("Senior", seniorCustomer.getCategory());
        assertEquals(10, seniorCustomer.getDiscount());

        assertEquals("Adult Premium", premiumCustomer.getCategory());
        assertEquals(15, premiumCustomer.getDiscount());
    }

    @Test
    public void testOrderProcessing() {
        KieSession kieSession = kieContainer.newKieSession();

        Customer seniorCustomer = new Customer(1L, "Charlie", 65, "charlie@example.com");
        seniorCustomer.setCategory("Senior");
        seniorCustomer.setDiscount(10);

        Order smallOrder = new Order(1L, 1L, new BigDecimal("500"), false, 0);
        Order largeOrder = new Order(2L, 1L, new BigDecimal("1500"), false, 0);
        Order bulkOrder = new Order(3L, 1L, new BigDecimal("6000"), false, 0);

        kieSession.insert(seniorCustomer);
        kieSession.insert(smallOrder);
        kieSession.insert(largeOrder);
        kieSession.insert(bulkOrder);

        kieSession.fireAllRules();
        kieSession.dispose();

        // Small order should just get the senior discount
        assertEquals(10, smallOrder.getDiscount());

        // Large order should get senior discount + high value discount
        assertEquals(15, largeOrder.getDiscount());

        // Bulk order should hit maximum discount cap
        assertEquals(30, bulkOrder.getDiscount());
        assertTrue(bulkOrder.isEligible());
    }
}
