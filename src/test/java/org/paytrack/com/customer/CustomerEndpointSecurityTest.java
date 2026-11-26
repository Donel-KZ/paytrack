package org.paytrack.com.customer;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.paytrack.com.common.exception.GlobalExceptionHandler;
import org.paytrack.com.customer.controller.CustomerController;
import org.paytrack.com.customer.dto.AddressDto;
import org.paytrack.com.customer.dto.CustomerCreateRequest;
import org.paytrack.com.customer.dto.CustomerResponse;
import org.paytrack.com.customer.dto.CustomerUpdateRequest;
import org.paytrack.com.customer.service.CustomerService;
import org.paytrack.com.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class CustomerEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private static final String VALID_GSTIN = "27ABCDE1234F1Z5";
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private AddressDto sampleBilling() {
        return new AddressDto("123 Nariman Point", "Mumbai", "Maharashtra", "400021", "India");
    }

    private AddressDto sampleShipping() {
        return new AddressDto("456 MIDC Phase 2", "Pune", "Maharashtra", "411018", "India");
    }

    private CustomerCreateRequest sampleCreateRequest() {
        return new CustomerCreateRequest("Acme Corp", VALID_GSTIN, sampleBilling(), sampleShipping());
    }

    private CustomerUpdateRequest sampleUpdateRequest() {
        return new CustomerUpdateRequest("Acme Corp Updated", VALID_GSTIN, sampleBilling(), sampleShipping());
    }

    private CustomerResponse sampleResponse() {
        return new CustomerResponse(CUSTOMER_ID, ORG_ID, "Acme Corp", VALID_GSTIN, sampleBilling(), sampleShipping());
    }

    // ==========================================
    // 1. POST /api/v1/customers (Create customer)
    // Permitted: OWNER, FINANCE_MANAGER, ACCOUNTANT
    // Denied: COLLECTOR, READ_ONLY
    // ==========================================
    @Nested
    @DisplayName("POST /api/v1/customers - Create Customer")
    class CreateCustomerSecurity {

        @Test
        @DisplayName("Permitted: OWNER role can create customer (201 Created)")
        @WithMockUser(roles = "OWNER")
        void ownerCanCreateCustomer() throws Exception {
            when(customerService.createCustomer(any(), any())).thenReturn(sampleResponse());

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Denied: COLLECTOR role cannot create customer (403 Forbidden)")
        @WithMockUser(roles = "COLLECTOR")
        void collectorCannotCreateCustomer() throws Exception {
            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // 2. GET /api/v1/customers (List customers)
    // Permitted: OWNER, FINANCE_MANAGER, ACCOUNTANT, COLLECTOR, READ_ONLY
    // Denied: Unauthenticated / Anonymous
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/customers - List Customers")
    class ListCustomersSecurity {

        @Test
        @DisplayName("Permitted: COLLECTOR role can list customers (200 OK)")
        @WithMockUser(roles = "COLLECTOR")
        void collectorCanListCustomers() throws Exception {
            when(customerService.listCustomers(any())).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Denied: Unauthenticated user cannot list customers (401 Unauthorized)")
        void unauthenticatedCannotListCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // 3. GET /api/v1/customers/{id} (Get customer by ID)
    // Permitted: READ_ONLY (and all 5 roles)
    // Denied: Unauthenticated / Anonymous
    // ==========================================
    @Nested
    @DisplayName("GET /api/v1/customers/{id} - View Customer")
    class GetCustomerSecurity {

        @Test
        @DisplayName("Permitted: READ_ONLY role can view customer (200 OK)")
        @WithMockUser(roles = "READ_ONLY")
        void readOnlyCanViewCustomer() throws Exception {
            when(customerService.getCustomer(any(), eq(CUSTOMER_ID))).thenReturn(sampleResponse());

            mockMvc.perform(get("/api/v1/customers/" + CUSTOMER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Denied: Unauthenticated user cannot view customer (401 Unauthorized)")
        void unauthenticatedCannotViewCustomer() throws Exception {
            mockMvc.perform(get("/api/v1/customers/" + CUSTOMER_ID))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // 4. PUT /api/v1/customers/{id} (Update customer)
    // Permitted: OWNER, FINANCE_MANAGER, ACCOUNTANT
    // Denied: COLLECTOR, READ_ONLY
    // ==========================================
    @Nested
    @DisplayName("PUT /api/v1/customers/{id} - Update Customer")
    class UpdateCustomerSecurity {

        @Test
        @DisplayName("Permitted: FINANCE_MANAGER role can update customer (200 OK)")
        @WithMockUser(roles = "FINANCE_MANAGER")
        void financeManagerCanUpdateCustomer() throws Exception {
            when(customerService.updateCustomer(any(), eq(CUSTOMER_ID), any())).thenReturn(sampleResponse());

            mockMvc.perform(put("/api/v1/customers/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest())))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Denied: COLLECTOR role cannot update customer (403 Forbidden)")
        @WithMockUser(roles = "COLLECTOR")
        void collectorCannotUpdateCustomer() throws Exception {
            mockMvc.perform(put("/api/v1/customers/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleUpdateRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // 5. DELETE /api/v1/customers/{id} (Delete customer)
    // Permitted: OWNER, FINANCE_MANAGER
    // Denied: ACCOUNTANT, COLLECTOR, READ_ONLY
    // ==========================================
    @Nested
    @DisplayName("DELETE /api/v1/customers/{id} - Delete Customer")
    class DeleteCustomerSecurity {

        @Test
        @DisplayName("Permitted: OWNER role can delete customer (204 No Content)")
        @WithMockUser(roles = "OWNER")
        void ownerCanDeleteCustomer() throws Exception {
            doNothing().when(customerService).deleteCustomer(any(), eq(CUSTOMER_ID));

            mockMvc.perform(delete("/api/v1/customers/" + CUSTOMER_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Denied: ACCOUNTANT role cannot delete customer (403 Forbidden)")
        @WithMockUser(roles = "ACCOUNTANT")
        void accountantCannotDeleteCustomer() throws Exception {
            mockMvc.perform(delete("/api/v1/customers/" + CUSTOMER_ID))
                    .andExpect(status().isForbidden());
        }
    }
}
