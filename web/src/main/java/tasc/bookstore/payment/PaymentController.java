package tasc.bookstore.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.response.ApiResponse;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vn-pay")
    public ApiResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        ApiResponse<PaymentDTO.VNPayResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(paymentService.createVnPayPayment(request));
        return apiResponse;
    }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        return paymentService.handleVnPayCallback(request);
    }

    @PostMapping("/query-order")
    public ApiResponse<VNPayQueryResponse> queryOrder(HttpServletRequest request) {
        VNPayQueryResponse response = paymentService.queryOrderStatus(request);
        paymentService.updatePaymentFromQueryDr(response);
        ApiResponse<VNPayQueryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }
}