package com.Krip4yk.oril_test_task.repository;

import com.Krip4yk.oril_test_task.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
}
