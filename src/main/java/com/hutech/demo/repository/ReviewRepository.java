package com.hutech.demo.repository;

import com.hutech.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndApprovedTrue(Long productId);
    List<Review> findByProductId(Long productId);
    List<Review> findByApprovedFalse();
    long countByProductIdAndApprovedTrue(Long productId);
}
