package tasc.bookstore.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.entity.CustomerPayment;
import tasc.bookstore.entity.Order;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.repository.CustomerPaymentRepository;
import tasc.bookstore.repository.OrderRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != Order.Status.PENDING) {
            throw new AppException(ErrorCode.NO_PAYMENT_EXISTED);
        }
        order.setStatus(Order.Status.PENDING_PAYMENT);

        BigDecimal total = order.getTotalAmount();

        long amount = total.multiply(new BigDecimal(100)).longValue();

        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        String vnpCreateDate = vnpParamsMap.get("vnp_CreateDate");
        order.setVnpCreateDate(vnpCreateDate);


        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
//        txnRef là viết tắt của Transaction Reference — tức là mã tham chiếu giao dịch.
        String txnRef = VNPayUtil.generateTxnRef(orderId);
        order.setVnpTxnRef(txnRef);
        orderRepository.save(order);
        vnpParamsMap.put("vnp_TxnRef", txnRef);
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getCode());
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
            Order order = orderRepository.findByVnpTxnRef(txnRef)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            // ✅ Chặn duplicate
            if (order.getStatus() == Order.Status.PAID) {
                apiResponse.setResult(new PaymentDTO.VNPayResponse("00", "Order already paid", null));
                return apiResponse;
            }

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

//            apiResponse.setMessage("Success");
            apiResponse.setResult(new PaymentDTO.VNPayResponse("00", "Success", null));
        } else {
            Order order = orderRepository.findByVnpTxnRef(txnRef)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            order.setStatus(Order.Status.PENDING);
            orderRepository.save(order);
            apiResponse.setMessage("Failed");
            apiResponse.setResult(null);
        }
        return apiResponse;
    }

    public VNPayQueryResponse queryOrderStatus(HttpServletRequest request) {
        // 1. Lấy orderId từ JSON body
        Long orderId;
        try {
            String jsonBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            Map<String, Object> bodyMap = new ObjectMapper().readValue(jsonBody, Map.class);
            orderId = Long.valueOf(bodyMap.get("orderId").toString());
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 2. Tìm order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String vnpTxnRef = order.getVnpTxnRef();
        String vnpTransactionDate = order.getVnpCreateDate();

        // 3. Sinh tham số
        String requestId = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String ipAddr = VNPayUtil.getIpAddress(request);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", vnPayConfig.getVnp_Version());
        params.put("vnp_Command", "querydr");
        params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_TransactionDate", vnpTransactionDate);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_OrderInfo", "Thong tin don hang:" + order.getCode());
        // 4. Ký hash
        String vnpSecureHash = VNPayUtil.hashAllFields(params, vnPayConfig.getSecretKey());
        params.put("vnp_SecureHash", vnpSecureHash);

        // 5. Gửi request JSON
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson;
        try {
            bodyJson = new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<VNPayQueryResponse> response = restTemplate.exchange(
                vnPayConfig.getVnp_ApiUrl(),
                HttpMethod.POST,
                entity,
                VNPayQueryResponse.class
        );

        return response.getBody();
    }

    public void updatePaymentFromQueryDr(VNPayQueryResponse response) {
        if (!"00".equals(response.getVnp_ResponseCode()) || !"00".equals(response.getVnp_TransactionStatus())) {
            // Payment không thành công hoặc query lỗi
            return;
        }

        String txnRef = response.getVnp_TxnRef();
        Order order = orderRepository.findByVnpTxnRef(txnRef)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chặn duplicate: nếu đã PAID thì bỏ qua
        if (order.getStatus() == Order.Status.PAID) {
            return;
        }

        // Update trạng thái order
        order.setStatus(Order.Status.PAID);
        orderRepository.save(order);

        // Lưu thông tin thanh toán
        CustomerPayment payment = new CustomerPayment();
        payment.setOrder(order);
        payment.setAmount(new BigDecimal(response.getVnp_Amount()).divide(new BigDecimal(100))); // VNPay gửi amount*100
        payment.setPaymentMethod(CustomerPayment.PaymentMethod.BANK_TRANSFER);
        payment.setPaidAt(LocalDateTime.parse(response.getVnp_PayDate(),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))); // parse từ vnp_PayDate

        customerPaymentRepository.save(payment);
    }



}