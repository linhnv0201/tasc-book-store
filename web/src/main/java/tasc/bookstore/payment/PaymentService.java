package tasc.bookstore.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.entity.CustomerPayment;
import tasc.bookstore.entity.Order;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.repository.CustomerPaymentRepository;
import tasc.bookstore.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    VNPAYConfig vnPayConfig;
    OrderRepository orderRepository;
    CustomerPaymentRepository customerPaymentRepository;

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        Long orderId = Long.parseLong(request.getParameter("orderId"));
        BigDecimal total = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getTotalAmount();
        long amount = total.multiply(new BigDecimal(100)).longValue();

        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", orderId.toString()); // Quan trọng để callback lấy orderId
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    public ApiResponse<PaymentDTO.VNPayResponse> handleVnPayCallback(HttpServletRequest request) {
        ApiResponse<PaymentDTO.VNPayResponse> apiResponse = new ApiResponse<>();

        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        if ("00".equals(vnpResponseCode) && txnRef != null) {
            Long orderId = Long.parseLong(txnRef);

            // Lấy order một lần
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            // Update order
            order.setStatus(Order.Status.PAID);
            orderRepository.save(order);

            // Tạo CustomerPayment
            CustomerPayment customerPayment = new CustomerPayment();
            customerPayment.setPaymentMethod(CustomerPayment.PaymentMethod.BANK_TRANSFER);
            customerPayment.setAmount(order.getTotalAmount());
            customerPayment.setPaidAt(LocalDateTime.now());
            customerPayment.setOrder(order);
            customerPaymentRepository.save(customerPayment);

            apiResponse.setMessage("Success");
            apiResponse.setResult(new PaymentDTO.VNPayResponse("00", "Success", ""));
        } else {
            apiResponse.setMessage("Failed");
            apiResponse.setResult(null);
        }

        return apiResponse;
    }

}