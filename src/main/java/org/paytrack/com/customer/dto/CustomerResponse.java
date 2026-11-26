package org.paytrack.com.customer.dto;

import org.paytrack.com.customer.domain.Customer;
import java.util.UUID;

public class CustomerResponse {

    private UUID id;
    private UUID organisationId;
    private String name;
    private String gstin;
    private AddressDto billingAddress;
    private AddressDto shippingAddress;

    public CustomerResponse() {
    }

    public CustomerResponse(UUID id, UUID organisationId, String name, String gstin, AddressDto billingAddress, AddressDto shippingAddress) {
        this.id = id;
        this.organisationId = organisationId;
        this.name = name;
        this.gstin = gstin;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }

    public static CustomerResponse fromDomain(Customer customer) {
        if (customer == null) return null;
        return new CustomerResponse(
            customer.getId(),
            customer.getOrganisationId(),
            customer.getName(),
            customer.getGstin(),
            AddressDto.fromDomain(customer.getBillingAddress()),
            AddressDto.fromDomain(customer.getShippingAddress())
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public AddressDto getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(AddressDto billingAddress) {
        this.billingAddress = billingAddress;
    }

    public AddressDto getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressDto shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
