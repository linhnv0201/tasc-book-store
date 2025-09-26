package tasc.bookstore.service;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tasc.bookstore.dto.request.AuthenticationRequest;
import tasc.bookstore.dto.request.IntrospectRequest;
import tasc.bookstore.dto.request.LogoutRequest;
import tasc.bookstore.dto.request.RefreshRequest;
import tasc.bookstore.dto.response.AuthenticationResponse;
import tasc.bookstore.dto.response.IntrospectResponse;
import tasc.bookstore.dto.response.RequireRefreshTokenResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest, HttpServletResponse response) throws ParseException, JOSEException;

    IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    RequireRefreshTokenResponse refreshToken(RefreshRequest request,HttpServletRequest httpRequest) throws ParseException, JOSEException;
}
