package tasc.bookstore.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    //AuthenticationRequest chính là thông tin đăng nhập(email + pass)
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //BCrypt khi mã hóa mật khẩu sẽ thực hiện nhiều vòng (rounds) hashing.
        //Số vòng = 2^strength.
        //Với strength = 10 → BCrypt chạy 2^10 = 1024 vòng tính toán để tạo ra hash.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.WRONG_PASSWORD);

        String token;
        try {
            token = generateToken(user);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            //chưa hiểu lắm vì sao lại true
            SignedJWT signedToken = verifyToken(request.getToken(), true);
            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jti).expireTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(jti).expireTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();

    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verifired = signedJWT.verify(verifier);

        if (!(verifired && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//        System.out.println(header); // in ra: {"alg":"HS512"} ("alg": thuật toán ký, ở đây là HS512 (HMAC + SHA-512))

//        đại diện phần payload
        JWTClaimsSet jwtClaimSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("vulinh")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("role", buildScope(user))
                .build();
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