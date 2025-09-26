package tasc.bookstore.controller;


import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import tasc.bookstore.dto.request.*;
import tasc.bookstore.dto.response.*;
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

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        AuthenticationResponse result = authenticationService.authenticate(request, response);
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/refresh")
    ApiResponse<RequireRefreshTokenResponse> refresh(
            @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) throws ParseException, JOSEException {

        // Truyền cả request để service đọc access token cũ
        var result = authenticationService.refreshToken(request,httpRequest);

        ApiResponse<RequireRefreshTokenResponse> apiResponse = new ApiResponse<>();
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