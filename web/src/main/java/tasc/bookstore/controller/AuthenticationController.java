package tasc.bookstore.controller;


import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.AuthenticationRequest;
import tasc.bookstore.dto.request.IntrospectRequest;
import tasc.bookstore.dto.request.LogoutRequest;
import tasc.bookstore.dto.request.UserCreationRequest;
import tasc.bookstore.dto.response.ApiResponse;
import tasc.bookstore.dto.response.AuthenticationResponse;
import tasc.bookstore.dto.response.IntrospectResponse;
import tasc.bookstore.dto.response.UserResponse;
import tasc.bookstore.service.AuthenticationService;
import tasc.bookstore.service.UserService;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        AuthenticationResponse result = authenticationService.authenticate(request);
        apiResponse.setResult(result);
        return apiResponse;
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(result)
//                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Register successful");
        apiResponse.setResult(userService.registerCustomer(request));
        return apiResponse;
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Logout successful");
        authenticationService.logout(request);
        return apiResponse;
    }

}