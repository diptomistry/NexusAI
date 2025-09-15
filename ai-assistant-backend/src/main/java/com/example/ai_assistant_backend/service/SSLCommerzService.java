package com.example.ai_assistant_backend.service;

import com.example.ai_assistant_backend.dto.PaymentRequest;
import com.example.ai_assistant_backend.dto.PaymentResponse;
import com.example.ai_assistant_backend.dto.PaymentStatusResponse;
import com.example.ai_assistant_backend.model.Payment;
import com.example.ai_assistant_backend.model.User;
import com.example.ai_assistant_backend.repository.PaymentRepository;
import com.example.ai_assistant_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@SuppressWarnings("deprecation")
public class SSLCommerzService {

    @Value("${sslcommerz.store.id:nexus68c2e3589bb36}")
    private String storeId;

    @Value("${sslcommerz.store.password:nexus68c2e3589bb36@ssl}")
    private String storePassword;

    @Value("${sslcommerz.sandbox:true}")
    private boolean sandbox;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${backend.url:http://localhost:8080}")
    private String backendUrl;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getApiUrl() {
        return sandbox ? "https://sandbox.sslcommerz.com" : "https://securepay.sslcommerz.com";
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            // Generate unique transaction ID
            String transactionId = generateTransactionId();

            // Create payment record in database
            Payment payment = new Payment();
            payment.setUserId(request.getUserIdAsUUID()); // Use helper method
            payment.setTransactionId(transactionId);
            payment.setAmount(request.getTotalAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus("pending");
            payment.setPlanType("pro_plan");
            payment.setCreditsAdded(500000);

            paymentRepository.save(payment);

            // Prepare SSLCommerz request
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("store_id", storeId));
            params.add(new BasicNameValuePair("store_passwd", storePassword));
            params.add(new BasicNameValuePair("total_amount", request.getTotalAmount().toString()));
            params.add(new BasicNameValuePair("currency", request.getCurrency()));
            params.add(new BasicNameValuePair("tran_id", transactionId));
            params.add(new BasicNameValuePair("success_url", backendUrl + "/api/payment/success"));
            params.add(new BasicNameValuePair("fail_url", backendUrl + "/api/payment/fail"));
            params.add(new BasicNameValuePair("cancel_url", backendUrl + "/api/payment/cancel"));
            params.add(new BasicNameValuePair("ipn_url", backendUrl + "/api/payment/ipn"));

            // Customer information
            params.add(new BasicNameValuePair("cus_name", request.getCustomerName()));
            params.add(new BasicNameValuePair("cus_email", request.getCustomerEmail()));
            params.add(new BasicNameValuePair("cus_add1", request.getCustomerAddress()));
            params.add(new BasicNameValuePair("cus_city", request.getCustomerCity()));
            params.add(new BasicNameValuePair("cus_state", request.getCustomerState()));
            params.add(new BasicNameValuePair("cus_postcode", request.getCustomerPostcode()));
            params.add(new BasicNameValuePair("cus_country", request.getCustomerCountry()));
            params.add(new BasicNameValuePair("cus_phone", request.getCustomerPhone()));

            // Product information
            params.add(new BasicNameValuePair("product_name", request.getProductName()));
            params.add(new BasicNameValuePair("product_category", request.getProductCategory()));
            params.add(new BasicNameValuePair("product_profile", request.getProductProfile()));

            // Shipping method (required)
            params.add(new BasicNameValuePair("shipping_method", "NO"));

            // Shipping information (same as customer for digital products)
            params.add(new BasicNameValuePair("ship_name", request.getCustomerName()));
            params.add(new BasicNameValuePair("ship_add1", request.getCustomerAddress()));
            params.add(new BasicNameValuePair("ship_city", request.getCustomerCity()));
            params.add(new BasicNameValuePair("ship_state", request.getCustomerState()));
            params.add(new BasicNameValuePair("ship_postcode", request.getCustomerPostcode()));
            params.add(new BasicNameValuePair("ship_country", request.getCustomerCountry()));

            // Make API call to SSLCommerz
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(getApiUrl() + "/gwprocess/v4/api.php");
                httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                CloseableHttpResponse response = httpClient.execute(httpPost);
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);

                    if ("SUCCESS".equals(jsonResponse.get("status").asText())) {
                        String gatewayPageUrl = jsonResponse.get("GatewayPageURL").asText();
                        return PaymentResponse.success("Payment initiated successfully", transactionId, gatewayPageUrl);
                    } else {
                        // Delete the payment record if initiation failed
                        paymentRepository.delete(payment);
                        return PaymentResponse
                                .failure("Payment initiation failed: " + jsonResponse.get("failedreason").asText());
                    }
                } finally {
                    response.close();
                }
            }

        } catch (Exception e) {
            return PaymentResponse.failure("Payment initiation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void handleIPN(Map<String, String> ipnData) {
        String transactionId = ipnData.get("tran_id");
        String status = ipnData.get("status");

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            // Update payment status based on IPN data
            if ("VALID".equals(status)) {
                payment.setStatus("success");
                updateUserCredits(payment);
            } else if ("FAILED".equals(status)) {
                payment.setStatus("failed");
            } else if ("CANCELLED".equals(status)) {
                payment.setStatus("cancelled");
            }

            // Store gateway response
            try {
                payment.setGatewayResponse(objectMapper.writeValueAsString(ipnData));
            } catch (Exception e) {
                System.err.println("Failed to serialize IPN data: " + e.getMessage());
            }

            paymentRepository.save(payment);
        }
    }

    @Transactional
    public void handlePaymentCallback(Map<String, String> callbackData, String callbackType) {
        String transactionId = callbackData.get("tran_id");
        String status = callbackData.get("status");

        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();

            switch (callbackType.toLowerCase()) {
                case "success":
                    if ("VALID".equals(status)) {
                        payment.setStatus("success");
                        updateUserCredits(payment);
                    }
                    break;
                case "fail":
                    payment.setStatus("failed");
                    break;
                case "cancel":
                    payment.setStatus("cancelled");
                    break;
            }

            // Extract and store payment method from callback data
            String paymentMethod = callbackData.get("card_type");
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                paymentMethod = callbackData.get("bank_tran_id") != null ? "Bank Transfer" : "Unknown";
            }
            payment.setPaymentMethod(paymentMethod);

            // Store gateway response
            try {
                payment.setGatewayResponse(objectMapper.writeValueAsString(callbackData));
            } catch (Exception e) {
                System.err.println("Failed to serialize callback data: " + e.getMessage());
            }

            paymentRepository.save(payment);
        }
    }

    private void updateUserCredits(Payment payment) {
        try {
            Optional<User> userOpt = userRepository.findById(payment.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setCredits(user.getCredits() + payment.getCreditsAdded());
                Integer currentMax = user.getMaxCredits() == null ? 0 : user.getMaxCredits();
                user.setMaxCredits(currentMax + payment.getCreditsAdded());
                user.setOrderId(payment.getTransactionId()); // Store transaction ID as order ID
                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("Failed to update user credits: " + e.getMessage());
        }
    }

    public PaymentStatusResponse getTransactionStatus(String transactionId) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            PaymentStatusResponse response = new PaymentStatusResponse();
            response.setTransactionId(payment.getTransactionId());
            response.setStatus(payment.getStatus());
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setPaymentMethod(payment.getPaymentMethod());
            response.setCreatedAt(payment.getCreatedAt());
            response.setMessage("Transaction found");
            return response;
        }
        return null;
    }

    public boolean validateTransaction(String transactionId) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("store_id", storeId));
            params.add(new BasicNameValuePair("store_passwd", storePassword));
            params.add(new BasicNameValuePair("val_id", transactionId));
            params.add(new BasicNameValuePair("format", "json"));

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(getApiUrl() + "/validator/api/validationserverAPI.php");
                httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                CloseableHttpResponse response = httpClient.execute(httpPost);
                try {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);

                    return "VALID".equals(jsonResponse.get("status").asText());
                } finally {
                    response.close();
                }
            }
        } catch (Exception e) {
            System.err.println("Transaction validation failed: " + e.getMessage());
            return false;
        }
    }

    private String generateTransactionId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String randomSuffix = String.valueOf(new Random().nextInt(999));
        return "TXN" + timestamp + randomSuffix;
    }
}
