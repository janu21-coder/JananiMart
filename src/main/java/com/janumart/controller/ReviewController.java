package com.janumart.controller;

import com.janumart.dto.ReviewRequest;
import com.janumart.model.User;
import com.janumart.service.ReviewService;
import com.janumart.util.JsonUtil;
import com.janumart.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** POST /api/v1/reviews (BUYER only; eligibility enforced server-side). */
public class ReviewController {

    private final ReviewService reviewService = new ReviewService();

    public void handle(HttpServletRequest req, HttpServletResponse resp, String method, List<String> seg)
            throws IOException {
        if (!seg.isEmpty()) {
            JsonUtil.error(resp, 404, "Review endpoint not found.");
            return;
        }
        if (!method.equals("POST")) {
            JsonUtil.error(resp, 405, "Method not allowed.");
            return;
        }
        User buyer = SessionUtil.requireRole(req, "BUYER");
        ReviewRequest rr = JsonUtil.parse(req, ReviewRequest.class);
        JsonUtil.created(resp, "Thank you! Your review has been posted.",
                reviewService.create(buyer, rr));
    }
}