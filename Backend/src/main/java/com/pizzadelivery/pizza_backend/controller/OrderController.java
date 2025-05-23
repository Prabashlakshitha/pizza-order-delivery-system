package com.pizzadelivery.pizza_backend.controller;

import com.pizzadelivery.pizza_backend.model.Order;
import com.pizzadelivery.pizza_backend.model.Order.OrderStatus;
import com.pizzadelivery.pizza_backend.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        String sessionUrl = orderService.createStripeSession(order);
        if (sessionUrl != null) {
            return ResponseEntity.ok(sessionUrl);
        } else {
            return ResponseEntity.status(500).body("Failed to create Stripe session");
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }


    @GetMapping("/getorder/{userName}")
    public ResponseEntity<List<Order>> getOrdersByUserName(@PathVariable String userName) {
        List<Order> orders = orderService.getOrdersByUserName(userName);
        return orders.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(orders);
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        String status = request.get("status"); // Extract status from the request body
        if (status == null) {
            return ResponseEntity.badRequest().body("Status is required!");
        }

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status); // Convert string to enum
            boolean updated = orderService.updateOrderStatus(id, orderStatus);
            if (updated) {
                return ResponseEntity.ok("Order status updated successfully!");
            } else {
                return ResponseEntity.badRequest().body("Order not found or status update failed!");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value!");
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable String id) {
        boolean deleted = orderService.deleteOrder(id);
        return deleted ? ResponseEntity.ok("Order deleted successfully!") : ResponseEntity.notFound().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");

        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid session ID"));
        }

        try {
            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);

            if (session != null && "paid".equals(session.getPaymentStatus())) {
                Map<String, String> metadata = session.getMetadata();
                if (metadata == null || !metadata.containsKey("order_id")) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing order ID"));
                }

                String orderId = metadata.get("order_id");
                Optional<Order> orderOpt = Optional.ofNullable(orderService.getOrderById(orderId));

                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
                    orderService.updatePaymentStatus(orderId, order.getPaymentStatus());
                    return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified"));
                }
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Payment not completed"));
        } catch (StripeException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Stripe API Error: " + e.getMessage()));
        }
    }
}
