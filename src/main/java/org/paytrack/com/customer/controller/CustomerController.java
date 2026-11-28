package org.paytrack.com.customer.controller;

import jakarta.validation.Valid;
import org.paytrack.com.customer.dto.CustomerCreateRequest;
import org.paytrack.com.customer.dto.CustomerResponse;
import org.paytrack.com.customer.dto.CustomerUpdateRequest;
import org.paytrack.com.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final UUID DEFAULT_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestHeader(value = "X-Organisation-Id", required = false) UUID orgHeader,
            @Valid @RequestBody CustomerCreateRequest request) {
        UUID organisationId = orgHeader != null ? orgHeader : DEFAULT_ORG_ID;
        CustomerResponse created = customerService.createCustomer(organisationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MANAGER', 'ACCOUNTANT', 'COLLECTOR', 'READ_ONLY')")
    public ResponseEntity<List<CustomerResponse>> listCustomers(
            @RequestHeader(value = "X-Organisation-Id", required = false) UUID orgHeader) {
        UUID organisationId = orgHeader != null ? orgHeader : DEFAULT_ORG_ID;
        return ResponseEntity.ok(customerService.listCustomers(organisationId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MANAGER', 'ACCOUNTANT', 'COLLECTOR', 'READ_ONLY')")
    public ResponseEntity<CustomerResponse> getCustomer(
            @RequestHeader(value = "X-Organisation-Id", required = false) UUID orgHeader,
            @PathVariable("id") UUID id) {
        UUID organisationId = orgHeader != null ? orgHeader : DEFAULT_ORG_ID;
        return ResponseEntity.ok(customerService.getCustomer(organisationId, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @RequestHeader(value = "X-Organisation-Id", required = false) UUID orgHeader,
            @PathVariable("id") UUID id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        UUID organisationId = orgHeader != null ? orgHeader : DEFAULT_ORG_ID;
        return ResponseEntity.ok(customerService.updateCustomer(organisationId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'FINANCE_MANAGER')")
    public ResponseEntity<Void> deleteCustomer(
            @RequestHeader(value = "X-Organisation-Id", required = false) UUID orgHeader,
            @PathVariable("id") UUID id) {
        UUID organisationId = orgHeader != null ? orgHeader : DEFAULT_ORG_ID;
        customerService.deleteCustomer(organisationId, id);
        return ResponseEntity.noContent().build();
    }
}
