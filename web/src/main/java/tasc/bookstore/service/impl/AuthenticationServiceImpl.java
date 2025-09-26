package tasc.bookstore.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tasc.bookstore.dto.request.AuthenticationRequest;
import tasc.bookstore.dto.request.IntrospectRequest;
import tasc.bookstore.dto.request.LogoutRequest;
import tasc.bookstore.dto.request.RefreshRequest;
import tasc.bookstore.dto.response.AuthenticationResponse;
import tasc.bookstore.dto.response.IntrospectResponse;
import tasc.bookstore.dto.response.RequireRefreshTokenResponse;
import tasc.bookstore.entity.InvalidatedToken;
import tasc.bookstore.entity.User;
import tasc.bookstore.exception.AppException;
import tasc.bookstore.exception.ErrorCode;
import tasc.bookstore.repository.InvalidatedTokenRepository;
import tasc.bookstore.repository.UserRepository;
import tasc.bookstore.service.AuthenticationService;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    //Nếu bạn thêm @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) ở class, thì
    //          tất cả field mặc định là final.
    //Nhưng @Value trong Spring tiêm giá trị sau khi object được khởi tạo → nếu field là final
    //          thì Spring không gán được nữa → lỗi.
    //Vì vậy Lombok cho annotation @NonFinal để override rule đó
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESHABLE_DURATION;

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    //AuthenticationRequest chính là thông tin đăng nhập(email + pass)
    @Override
    public AuthenticationResponse authenticate(
            AuthenticationRequest request,
            HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //BCrypt khi mã hóa mật khẩu sẽ thực hiện nhiều vòng (rounds) hashing.
        //Số vòng = 2^strength.
        //Với strength = 10 → BCrypt chạy 2^10 = 1024 vòng tính toán để tạo ra hash.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.WRONG_PASSWORD);

        String accessToken;
        String refreshToken;
        try {
            accessToken = generateToken(user, VALID_DURATION, "ACCESS");
            refreshToken = generateToken(user, REFRESHABLE_DURATION, "REFRESH");
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        // --- Set access token cookie ---
        jakarta.servlet.http.Cookie accessCookie = new jakarta.servlet.http.Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);  // JS không đọc được
        accessCookie.setPath("/");        // áp dụng toàn bộ domain
        accessCookie.setMaxAge(VALID_DURATION.intValue()); // thời gian sống token (giây)
        response.addCookie(accessCookie);

        // --- Set refresh token cookie ---
        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);      // JS không đọc được
        refreshCookie.setPath("/auth/refresh"); // chỉ gửi khi gọi endpoint refresh
        refreshCookie.setMaxAge(REFRESHABLE_DURATION.intValue()); // thời gian sống refresh token
        response.addCookie(refreshCookie);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedToken = verifyToken(request.getToken());
            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jti).expireTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    @Override
    public RequireRefreshTokenResponse refreshToken(RefreshRequest request, HttpServletRequest httpRequest) throws ParseException, JOSEException {
        String refreshToken = request.getToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 2. Verify refresh token
        SignedJWT refreshJWT = verifyToken(refreshToken);
        String username = refreshJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Invalidate old access token (lấy từ header Authorization)
        String oldAccessToken = getAccessTokenFromRequest(httpRequest);
        System.out.println(oldAccessToken);
        if (oldAccessToken != null) {
            SignedJWT oldAccessJWT = SignedJWT.parse(oldAccessToken);
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(oldAccessJWT.getJWTClaimsSet().getJWTID())
                    .expireTime(oldAccessJWT.getJWTClaimsSet().getExpirationTime())
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        }

        // 4. Tạo access token mới
        String newAccessToken = generateToken(user, VALID_DURATION, "ACCESS");

        return RequireRefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .authenticated(true)
                .build();
    }

    private String getAccessTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }



    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean verifired = signedJWT.verify(verifier);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!(verifired && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    String generateToken(User user, Long duration, String type) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//        đại diện phần payload
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("vulinh")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", type); // ACCESS vs REFRESH

        if ("ACCESS".equals(type)) {
            claimsBuilder.claim("role", buildScope(user));
        }
        // Build claims cuối cùng
        JWTClaimsSet jwtClaimSet = claimsBuilder.build();

//        in ra: {"sub":"king","scope":"EMPLOYEE CUSTOMER ADMIN","iss":"vulinh"
//                ,"exp":1757582258,"iat":1757578658,"jti":"48a34366-d69d-4e1c-aff3-7f2579ecc165"}
//        System.out.println(jwtClaimSet);

//         chuyển từ dạng JWTClaimSet sang JSONObject
//        Payload là object mà Nimbus dùng để đưa dữ liệu payload vào JWSObject.
//        jwtClaimSet.toJSONObject() → convert claims thành JSONObject.
        Payload payload = new Payload(jwtClaimSet.toJSONObject());


//        JSON Web Signature Object
//        Đại diện cho một JWT đã ký (Signed JWT) trong thư viện Nimbus
        JWSObject jwsObject = new JWSObject(header, payload);
//        System.out.println(jwsObject); //in ra: com.nimbusds.jose.JWSObject@1b9bba97

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
//            System.out.println(jwsObject.serialize());
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    //      buildScope dùng để tạo ra chuỗi “scope” cho JWT dựa trên role của user.
//      Scope thường được dùng trong JWT để xác định quyền hạn / permission của người dùng.
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRole())) {
            user.getRole().forEach(role -> stringJoiner.add(role.name())); // dùng .name() chuyển enum -> String
        }
        return stringJoiner.toString();
    }

}