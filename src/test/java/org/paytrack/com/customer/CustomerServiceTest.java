package org.paytrack.com.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.paytrack.com.common.exception.ResourceNotFoundException;
import org.paytrack.com.customer.domain.Customer;
import org.paytrack.com.customer.dto.AddressDto;
import org.paytrack.com.customer.dto.CustomerCreateRequest;
import org.paytrack.com.customer.dto.CustomerResponse;
import org.paytrack.com.customer.dto.CustomerUpdateRequest;
import org.paytrack.com.customer.repository.CustomerRepository;
import org.paytrack.com.customer.service.CustomerService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_GSTIN = "27ABCDE1234F1Z5";

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    private AddressDto sampleBillingDto() {
        return new AddressDto("123 Billing St", "Mumbai", "Maharashtra", "400001", "India");
    }

    private AddressDto sampleShippingDto() {
        return new AddressDto("456 Shipping Way", "Pune", "Maharashtra", "411001", "India");
    }

    private Customer sampleCustomer() {
        Customer c = new Customer(
            ORG_ID,
            "Alpha Corp",
            VALID_GSTIN,
            sampleBillingDto().toDomain(),
            sampleShippingDto().toDomain()
        );
        c.setId(CUSTOMER_ID);
        return c;
    }

    @Test
    @DisplayName("createCustomer saves and returns customer response with separate addresses")
    void createCustomer_Success() {
        CustomerCreateRequest request = new CustomerCreateRequest(
            "Alpha Corp",
            VALID_GSTIN,
            sampleBillingDto(),
            sampleShippingDto()
        );

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer toSave = invocation.getArgument(0);
            toSave.setId(CUSTOMER_ID);
            return toSave;
        });

        CustomerResponse response = customerService.createCustomer(ORG_ID, request);

        assertNotNull(response);
        assertEquals(CUSTOMER_ID, response.getId());
        assertEquals("Alpha Corp", response.getName());
        assertEquals(VALID_GSTIN, response.getGstin());
        assertEquals("Mumbai", response.getBillingAddress().getCity());
        assertEquals("Pune", response.getShippingAddress().getCity());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("getCustomer returns customer if found")
    void getCustomer_Found() {
        Customer customer = sampleCustomer();
        when(customerRepository.findByIdAndOrganisationId(CUSTOMER_ID, ORG_ID))
            .thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomer(ORG_ID, CUSTOMER_ID);

        assertNotNull(response);
        assertEquals(CUSTOMER_ID, response.getId());
        assertEquals("Alpha Corp", response.getName());
    }

    @Test
    @DisplayName("getCustomer throws ResourceNotFoundException if customer not found")
    void getCustomer_NotFound() {
        when(customerRepository.findByIdAndOrganisationId(CUSTOMER_ID, ORG_ID))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomer(ORG_ID, CUSTOMER_ID));
    }

    @Test
    @DisplayName("listCustomers returns all customers in organisation")
    void listCustomers_Success() {
        when(customerRepository.findAllByOrganisationId(ORG_ID))
            .thenReturn(List.of(sampleCustomer()));

        List<CustomerResponse> list = customerService.listCustomers(ORG_ID);

        assertEquals(1, list.size());
        assertEquals("Alpha Corp", list.get(0).getName());
    }

    @Test
    @DisplayName("updateCustomer updates fields and saves")
    void updateCustomer_Success() {
        Customer customer = sampleCustomer();
        when(customerRepository.findByIdAndOrganisationId(CUSTOMER_ID, ORG_ID))
            .thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
            "Alpha Corp Renamed",
            "29ABCDE1234F1Z8",
            new AddressDto("New Street", "Bengaluru", "Karnataka", "560001", "India"),
            sampleShippingDto()
        );

        CustomerResponse updated = customerService.updateCustomer(ORG_ID, CUSTOMER_ID, updateRequest);

        assertNotNull(updated);
        assertEquals("Alpha Corp Renamed", updated.getName());
        assertEquals("29ABCDE1234F1Z8", updated.getGstin());
        assertEquals("Bengaluru", updated.getBillingAddress().getCity());
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("deleteCustomer deletes when customer exists")
    void deleteCustomer_Success() {
        when(customerRepository.existsByIdAndOrganisationId(CUSTOMER_ID, ORG_ID)).thenReturn(true);
        doNothing().when(customerRepository).deleteByIdAndOrganisationId(CUSTOMER_ID, ORG_ID);

        assertDoesNotThrow(() -> customerService.deleteCustomer(ORG_ID, CUSTOMER_ID));
        verify(customerRepository).deleteByIdAndOrganisationId(CUSTOMER_ID, ORG_ID);
    }

    @Test
    @DisplayName("deleteCustomer throws ResourceNotFoundException when customer does not exist")
    void deleteCustomer_NotFound() {
        when(customerRepository.existsByIdAndOrganisationId(CUSTOMER_ID, ORG_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(ORG_ID, CUSTOMER_ID));
        verify(customerRepository, never()).deleteByIdAndOrganisationId(any(), any());
    }
}
