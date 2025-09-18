package tasc.bookstore.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tasc.bookstore.dto.response.ApiResponse;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

        @GetMapping("/vn-pay")
        public ApiResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
            ApiResponse<PaymentDTO.VNPayResponse> apiResponse = new ApiResponse<>();
            apiResponse.setMessage("Success");
            apiResponse.setResult(paymentService.createVnPayPayment(request));
            return apiResponse;
        }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        return paymentService.handleVnPayCallback(request);
    }

}