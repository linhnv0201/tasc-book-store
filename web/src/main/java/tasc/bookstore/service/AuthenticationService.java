package tasc.bookstore.service;

import com.nimbusds.jose.JOSEException;
import tasc.bookstore.dto.request.AuthenticationRequest;
import tasc.bookstore.dto.request.IntrospectRequest;
import tasc.bookstore.dto.request.LogoutRequest;
import tasc.bookstore.dto.response.AuthenticationResponse;
import tasc.bookstore.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException;
    void logout(LogoutRequest request) throws ParseException, JOSEException;
//    String generateToken(String email);
}
