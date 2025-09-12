package com.example.ai_assistant_backend.controller;

import com.example.ai_assistant_backend.dto.PaymentRequest;
import com.example.ai_assistant_backend.dto.PaymentResponse;
import com.example.ai_assistant_backend.dto.PaymentStatusResponse;
import com.example.ai_assistant_backend.service.SSLCommerzService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class PaymentController {

    @Autowired
    private SSLCommerzService sslCommerzService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        try {
            System.out.println("Received payment request for user: " + request.getUserId());
            System.out.println("Amount: " + request.getTotalAmount());

            PaymentResponse response = sslCommerzService.initiatePayment(request);

            if (response.isSuccess()) {
                System.out.println("Payment initiation successful");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Payment initiation failed: " + response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.out.println("Payment initiation error: " + e.getMessage());
            e.printStackTrace();
            PaymentResponse errorResponse = PaymentResponse.failure("Payment initiation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/ipn")
    @CrossOrigin(origins = "*") // Allow all origins for SSLCommerz IPN
    public ResponseEntity<String> handleIPN(@RequestParam Map<String, String> ipnData) {
        try {
            sslCommerzService.handleIPN(ipnData);
            return ResponseEntity.ok("IPN processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IPN processing failed");
        }
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String transactionId) {
        try {
            PaymentStatusResponse response = sslCommerzService.getTransactionStatus(transactionId);

            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/validate/{transactionId}")
    public ResponseEntity<Map<String, Object>> validateTransaction(@PathVariable String transactionId) {
        try {
            boolean isValid = sslCommerzService.validateTransaction(transactionId);

            Map<String, Object> response = Map.of(
                    "transactionId", transactionId,
                    "valid", isValid,
                    "message", isValid ? "Transaction is valid" : "Transaction is invalid");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "transactionId", transactionId,
                    "valid", false,
                    "message", "Validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Handle redirect URLs from SSLCommerz (SSLCommerz sends POST requests to these
    // URLs)
    @PostMapping("/success")
    @CrossOrigin(origins = "*") // Allow all origins for SSLCommerz callbacks
    public ResponseEntity<Void> handleSuccessPost(@RequestParam Map<String, String> params) {
        String transactionId = params.get("tran_id");
        String status = params.get("status");

        // Log the success parameters for debugging
        System.out.println("Success callback received - Transaction ID: " + transactionId + ", Status: " + status);

        // Update transaction status
        sslCommerzService.handlePaymentCallback(params, "success");

        String frontendRedirectUrl = frontendUrl + "/payment/success?tran_id=" + transactionId + "&status=" + status;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendRedirectUrl)
                .build();
    }

    // Also keep GET method for direct access
    @GetMapping("/success")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> handleSuccessGet(@RequestParam Map<String, String> params) {
        return handleSuccessPost(params);
    }

    @PostMapping("/fail")
    @CrossOrigin(origins = "*") // Allow all origins for SSLCommerz callbacks
    public ResponseEntity<Void> handleFailurePost(@RequestParam Map<String, String> params) {
        String transactionId = params.get("tran_id");
        String status = params.get("status");
        String failedReason = params.get("failedreason");

        // Log the failure parameters for debugging
        System.out.println("Failure callback received - Transaction ID: " + transactionId +
                ", Status: " + status + ", Reason: " + failedReason);

        // Update transaction status
        sslCommerzService.handlePaymentCallback(params, "fail");

        String frontendRedirectUrl = frontendUrl + "/payment/fail?tran_id=" + transactionId + "&reason=" + failedReason;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendRedirectUrl)
                .build();
    }

    @GetMapping("/fail")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> handleFailureGet(@RequestParam Map<String, String> params) {
        return handleFailurePost(params);
    }

    @PostMapping("/cancel")
    @CrossOrigin(origins = "*") // Allow all origins for SSLCommerz callbacks
    public ResponseEntity<Void> handleCancelPost(@RequestParam Map<String, String> params) {
        String transactionId = params.get("tran_id");
        String status = params.get("status");

        // Log the cancellation parameters for debugging
        System.out.println("Cancel callback received - Transaction ID: " + transactionId + ", Status: " + status);

        // Update transaction status
        sslCommerzService.handlePaymentCallback(params, "cancel");

        String frontendRedirectUrl = frontendUrl + "/payment/cancel?tran_id=" + transactionId;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendRedirectUrl)
                .build();
    }

    @GetMapping("/cancel")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> handleCancelGet(@RequestParam Map<String, String> params) {
        return handleCancelPost(params);
    }
}
