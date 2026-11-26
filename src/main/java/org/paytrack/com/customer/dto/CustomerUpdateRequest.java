package org.paytrack.com.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CustomerUpdateRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "GSTIN is required")
    @Pattern(regexp = CustomerCreateRequest.GSTIN_REGEX, message = CustomerCreateRequest.GSTIN_ERROR_MESSAGE)
    private String gstin;

    @NotNull(message = "Billing address is required")
    @Valid
    private AddressDto billingAddress;

    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDto shippingAddress;

    public CustomerUpdateRequest() {
    }

    public CustomerUpdateRequest(String name, String gstin, AddressDto billingAddress, AddressDto shippingAddress) {
        this.name = name;
        this.gstin = gstin;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
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
