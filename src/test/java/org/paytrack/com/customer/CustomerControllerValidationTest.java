package org.paytrack.com.customer;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.paytrack.com.common.exception.GlobalExceptionHandler;
import org.paytrack.com.customer.controller.CustomerController;
import org.paytrack.com.customer.dto.AddressDto;
import org.paytrack.com.customer.dto.CustomerCreateRequest;
import org.paytrack.com.customer.dto.CustomerResponse;
import org.paytrack.com.customer.dto.CustomerUpdateRequest;
import org.paytrack.com.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CustomerControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private static final String VALID_GSTIN = "27ABCDE1234F1Z5";
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    private AddressDto sampleBillingAddress() {
        return new AddressDto("123 Nariman Point", "Mumbai", "Maharashtra", "400021", "India");
    }

    private AddressDto sampleShippingAddress() {
        return new AddressDto("456 MIDC Phase 2", "Pune", "Maharashtra", "411018", "India");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "INVALID_GSTIN",        // Not matching format
        "12345",                // Too short
        "27ABCDE1234F1Z",       // 14 chars (too short)
        "27ABCDE1234F1Z59",     // 16 chars (too long)
        "27abcde1234f1z5",      // Lowercase not allowed
        "27ABCDE123451Z5",      // 5th character of PAN cannot be numeric
        "27ABCDE1234F1A5"       // 14th character must be 'Z'
    })
    @DisplayName("Create customer with invalid GSTIN should return 400 Bad Request with validation error")
    @WithMockUser(roles = "OWNER")
    void shouldReturn400WhenCreateCustomerWithInvalidGstin(String invalidGstin) throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest(
            "Tech Innovations Ltd",
            invalidGstin,
            sampleBillingAddress(),
            sampleShippingAddress()
        );

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("gstin"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BAD_GSTIN_123",
        "27ABCDE1234F1A5",
        "INVALID"
    })
    @DisplayName("Update customer with invalid GSTIN should return 400 Bad Request with validation error")
    @WithMockUser(roles = "OWNER")
    void shouldReturn400WhenUpdateCustomerWithInvalidGstin(String invalidGstin) throws Exception {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "Tech Innovations Ltd Updated",
            invalidGstin,
            sampleBillingAddress(),
            sampleShippingAddress()
        );

        mockMvc.perform(put("/api/v1/customers/" + CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("gstin"));
    }

    @Test
    @DisplayName("Create customer with valid GSTIN should succeed with 201 Created")
    @WithMockUser(roles = "OWNER")
    void shouldReturn201WhenCreateCustomerWithValidGstin() throws Exception {
        CustomerCreateRequest request = new CustomerCreateRequest(
            "Tech Innovations Ltd",
            VALID_GSTIN,
            sampleBillingAddress(),
            sampleShippingAddress()
        );

        CustomerResponse response = new CustomerResponse(
            CUSTOMER_ID,
            ORG_ID,
            request.getName(),
            request.getGstin(),
            request.getBillingAddress(),
            request.getShippingAddress()
        );

        when(customerService.createCustomer(any(UUID.class), any(CustomerCreateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.gstin").value(VALID_GSTIN))
                .andExpect(jsonPath("$.name").value("Tech Innovations Ltd"));
    }

    @Test
    @DisplayName("Update customer with valid GSTIN should succeed with 200 OK")
    @WithMockUser(roles = "OWNER")
    void shouldReturn200WhenUpdateCustomerWithValidGstin() throws Exception {
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "Tech Innovations Ltd Updated",
            VALID_GSTIN,
            sampleBillingAddress(),
            sampleShippingAddress()
        );

        CustomerResponse response = new CustomerResponse(
            CUSTOMER_ID,
            ORG_ID,
            request.getName(),
            request.getGstin(),
            request.getBillingAddress(),
            request.getShippingAddress()
        );

        when(customerService.updateCustomer(any(UUID.class), eq(CUSTOMER_ID), any(CustomerUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put("/api/v1/customers/" + CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.gstin").value(VALID_GSTIN))
                .andExpect(jsonPath("$.name").value("Tech Innovations Ltd Updated"));
    }
}
