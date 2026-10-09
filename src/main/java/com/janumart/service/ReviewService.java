package com.janumart.service;

import com.janumart.dao.ProductDao;
import com.janumart.dao.ReviewDao;
import com.janumart.dto.ReviewRequest;
import com.janumart.exception.ForbiddenException;
import com.janumart.exception.NotFoundException;
import com.janumart.exception.ValidationException;
import com.janumart.model.Product;
import com.janumart.model.Review;
import com.janumart.model.User;
import com.janumart.util.Validate;

import java.util.List;

/**
 * Reviews: only buyers with a DELIVERED order containing the product may
 * review, and only once per product.
 */
public class ReviewService {

    private final ReviewDao reviewDao = new ReviewDao();
    private final ProductDao productDao = new ProductDao();

    public Review create(User buyer, ReviewRequest req) {
        if (req.getProductId() == null) {
            throw new ValidationException("Product id is required.");
        }
        Validate.positiveId(req.getProductId(), "Product id");
        if (req.getRating() == null) {
            throw new ValidationException("Rating is required.");
        }
        Validate.rating(req.getRating());

        Product product = productDao.findById(req.getProductId());
        if (product == null) {
            throw new NotFoundException("Product not found.");
        }

        if (!reviewDao.hasDeliveredPurchase(buyer.getId(), req.getProductId())) {
            throw new ForbiddenException(
                    "You can review a product only after it has been delivered to you.");
        }
        if (reviewDao.exists(buyer.getId(), req.getProductId())) {
            throw new ValidationException("You have already reviewed this product.");
        }

        Review review = new Review();
        review.setProductId(req.getProductId());
        review.setUserId(buyer.getId());
        review.setRating(req.getRating());
        review.setComment(Validate.option(req.getComment(), "Comment", 1000));
        reviewDao.insert(review);
        return review;
    }

    public List<Review> forProduct(int productId) {
        Validate.positiveId(productId, "Product id");
        return reviewDao.findByProduct(productId);
    }
}