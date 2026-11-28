package org.paytrack.com.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CustomerCreateRequest {

    public static final String GSTIN_REGEX = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
    public static final String GSTIN_ERROR_MESSAGE = "Invalid GSTIN format. Expected 15 characters: 2 digits state code, 10 characters PAN, 1 entity code, 'Z', and 1 check digit.";

    @NotBlank(message = "Customer name is required")
    private String name;

    @NotBlank(message = "GSTIN is required")
    @Pattern(regexp = GSTIN_REGEX, message = GSTIN_ERROR_MESSAGE)
    private String gstin;

    @NotNull(message = "Billing address is required")
    @Valid
    private AddressDto billingAddress;

    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDto shippingAddress;

    public CustomerCreateRequest() {
    }

    public CustomerCreateRequest(String name, String gstin, AddressDto billingAddress, AddressDto shippingAddress) {
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
