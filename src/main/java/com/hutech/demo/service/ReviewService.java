package com.hutech.demo.service;

import com.hutech.demo.model.Review;
import com.hutech.demo.model.User;
import com.hutech.demo.model.Product;
import com.hutech.demo.repository.ReviewRepository;
import com.hutech.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Review addReview(User user, Long productId, int rating, String comment) {
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(optProduct.get());
        review.setRating(rating);
        review.setComment(comment);
        review.setApproved(false); // Chờ admin duyệt
        return reviewRepository.save(review);
    }

    public List<Review> getApprovedReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrue(productId);
    }

    public List<Review> getAllReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getPendingReviews() {
        return reviewRepository.findByApprovedFalse();
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public boolean approveReview(Long reviewId) {
        Optional<Review> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty()) return false;
        Review review = opt.get();
        review.setApproved(true);
        reviewRepository.save(review);
        return true;
    }

    public boolean deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) return false;
        reviewRepository.deleteById(reviewId);
        return true;
    }

    public double getAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndApprovedTrue(productId);
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }
}
