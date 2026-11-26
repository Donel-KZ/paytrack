package org.paytrack.com.customer.repository;

import org.paytrack.com.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByOrganisationId(UUID organisationId);

    Optional<Customer> findByIdAndOrganisationId(UUID id, UUID organisationId);

    void deleteByIdAndOrganisationId(UUID id, UUID organisationId);

    boolean existsByIdAndOrganisationId(UUID id, UUID organisationId);
}
