package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.model.User;
import com.example.ai_assistant_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class SubscriptionController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/cancel-subscription")
    public ResponseEntity<Map<String, Object>> cancelSubscription(@RequestBody Map<String, Object> request) {
        try {
            String userIdStr = (String) request.get("userId");

            if (userIdStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User ID is required"));
            }

            UUID userId = UUID.fromString(userIdStr);
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setOrderId(null); // Remove order ID to downgrade to free plan
                // No credit limit reset on cancel as per requirement
                userRepository.save(user);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Subscription cancelled successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to cancel subscription: " + e.getMessage()));
        }
    }
}
