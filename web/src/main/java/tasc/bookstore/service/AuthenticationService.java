package tasc.bookstore.service;

import tasc.bookstore.dto.request.AuthenticationRequest;

public interface AuthenticationService {
    boolean authenticate(AuthenticationRequest authenticationRequest);
}
