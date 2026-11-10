package com.hacka.Hacka.repository;

import com.hacka.Hacka.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findFirstByCityIgnoreCaseAndAddressIgnoreCase(String city, String address);
}
