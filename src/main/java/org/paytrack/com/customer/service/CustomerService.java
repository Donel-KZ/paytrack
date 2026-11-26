package org.paytrack.com.customer.service;

import org.paytrack.com.common.exception.ResourceNotFoundException;
import org.paytrack.com.customer.domain.Customer;
import org.paytrack.com.customer.dto.CustomerCreateRequest;
import org.paytrack.com.customer.dto.CustomerResponse;
import org.paytrack.com.customer.dto.CustomerUpdateRequest;
import org.paytrack.com.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(UUID organisationId, CustomerCreateRequest request) {
        Customer customer = new Customer(
            organisationId,
            request.getName(),
            request.getGstin(),
            request.getBillingAddress().toDomain(),
            request.getShippingAddress().toDomain()
        );
        Customer saved = customerRepository.save(customer);
        return CustomerResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers(UUID organisationId) {
        return customerRepository.findAllByOrganisationId(organisationId).stream()
            .map(CustomerResponse::fromDomain)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID organisationId, UUID customerId) {
        Customer customer = customerRepository.findByIdAndOrganisationId(customerId, organisationId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return CustomerResponse.fromDomain(customer);
    }

    public CustomerResponse updateCustomer(UUID organisationId, UUID customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByIdAndOrganisationId(customerId, organisationId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customer.setName(request.getName());
        customer.setGstin(request.getGstin());
        customer.setBillingAddress(request.getBillingAddress().toDomain());
        customer.setShippingAddress(request.getShippingAddress().toDomain());

        Customer updated = customerRepository.save(customer);
        return CustomerResponse.fromDomain(updated);
    }

    public void deleteCustomer(UUID organisationId, UUID customerId) {
        if (!customerRepository.existsByIdAndOrganisationId(customerId, organisationId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        customerRepository.deleteByIdAndOrganisationId(customerId, organisationId);
    }
}
