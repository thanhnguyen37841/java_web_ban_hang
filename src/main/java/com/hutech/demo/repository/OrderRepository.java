package com.hutech.demo.repository;

import com.hutech.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByStatus(String status);
    List<Order> findAllByOrderByOrderDateDesc();
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(o.totalAmount - o.discountAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount - o.discountAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    double getTotalSales();
}
