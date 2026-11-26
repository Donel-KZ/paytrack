package org.paytrack.com.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.paytrack.com.customer.domain.Address;
import org.paytrack.com.customer.domain.Customer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerAddressSeparationTest {

    @Test
    @DisplayName("Customer records store billing and shipping addresses separately")
    void shouldStoreBillingAndShippingAddressesSeparately() {
        UUID orgId = UUID.randomUUID();
        String validGstin = "27ABCDE1234F1Z5";

        Address billingAddress = new Address(
            "100 Nariman Point, Floor 12",
            "Mumbai",
            "Maharashtra",
            "400021",
            "India"
        );

        Address shippingAddress = new Address(
            "Plot 50, MIDC Industrial Area, Phase II",
            "Pune",
            "Maharashtra",
            "411018",
            "India"
        );

        Customer customer = new Customer(
            orgId,
            "Tata Consulting Services",
            validGstin,
            billingAddress,
            shippingAddress
        );

        // Verify that billing and shipping address references and values are distinct
        assertNotNull(customer.getBillingAddress(), "Billing address must not be null");
        assertNotNull(customer.getShippingAddress(), "Shipping address must not be null");
        assertNotSame(customer.getBillingAddress(), customer.getShippingAddress(), "Billing and Shipping address instances must be separate");

        // Verify field separation
        assertEquals("Mumbai", customer.getBillingAddress().getCity());
        assertEquals("Pune", customer.getShippingAddress().getCity());
        assertEquals("400021", customer.getBillingAddress().getPostalCode());
        assertEquals("411018", customer.getShippingAddress().getPostalCode());
        assertEquals("100 Nariman Point, Floor 12", customer.getBillingAddress().getStreet());
        assertEquals("Plot 50, MIDC Industrial Area, Phase II", customer.getShippingAddress().getStreet());

        // Verify mutating billing address does not mutate shipping address
        customer.getBillingAddress().setCity("Nagpur");
        assertEquals("Nagpur", customer.getBillingAddress().getCity());
        assertEquals("Pune", customer.getShippingAddress().getCity(), "Shipping city must remain Pune");
    }
}
