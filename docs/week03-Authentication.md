# 🔐 Authentication & Authorization – Spring Boot + JWT

## 📌 Mục tiêu
- Tạo API Đăng ký (`/api/auth/register`) và Đăng nhập (`/api/auth/login`).
- Mã hóa mật khẩu bằng `BCryptPasswordEncoder`.
- Tích hợp JWT để sinh và xác thực token.
- Bảo vệ endpoint `/api/tasks` bằng token.

---

## 1. Cấu hình bảo mật – `SecurityConfig.java`
📁 [SecurityConfig.java](../src/main/java/com/taskmanagement/config/SecurityConfig.java)
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 2. Controller – `AuthController.java`
📁 [AuthController.java](../src/main/java/com/taskmanagement/controller/AuthController.java)
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public SuccessResponse register(@RequestBody @Valid RegisterRequest dto) {
        var data = authService.register(dto);
        return new SuccessResponse(data);
    }

    @PostMapping("/login")
    public SuccessResponse login(@RequestBody @Valid LoginRequest dto) {
        var data = authService.login(dto);
        return new SuccessResponse(data);
    }
}
```

---

## 3. Service Interface – `AuthService.java`
📁 [AuthService.java](../src/main/java/com/taskmanagement/service/AuthService.java)

```java
public interface AuthService {
    UserResponseDTO register(RegisterRequest dto);
    LoginResponse login(LoginRequest dto);
}
```

---

## 4. Service Implementation – `AuthServiceImpl.java`
📁 [AuthServiceImpl.java](../src/main/java/com/taskmanagement/service/impl/AuthServiceImpl.java)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserResponseDTO register(RegisterRequest dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new AppException(ExceptionCode.USER_FOUND);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreated(Instant.now());
        user.setRole(Role.fromString(dto.getRole()));
        userRepository.save(user);

        UserResponseDTO dtoResponse = userMapper.toUserResponseDTO(user);
        if (!user.getRole().equals(Role.ADMIN)) dtoResponse.setRole(null);
        return dtoResponse;
    }

    @Override
    public LoginResponse login(LoginRequest dto) {
        User user = userRepository.findByUsername(dto.getUsername())
            .orElseThrow(() -> new AppException(ExceptionCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new AppException(ExceptionCode.PASSWORD_INCORRECT);
        }

        String token = jwtTokenProvider.generateToken(new CustomUserDetail(user));
        return new LoginResponse(token);
    }
}
```

---

## 5. JWT Token – `JwtTokenProvider.java`
📁 [JwtTokenProvider.java](../src/main/java/com/taskmanagement/security/JwtTokenProvider.java)
```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## 6. JWT Filter – `JwtAuthenticationFilter.java`
📁 [JwtAuthenticationFilter.java](../src/main/java/com/taskmanagement/security/JwtAuthenticationFilter.java)
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                var userDetails = customUserDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 7. DTOs

### `LoginRequest.java`
📁 [LoginRequest.java](../src/main/java/com/taskmanagement/dto/auth/LoginRequest.java)
```java
@Data @AllArgsConstructor @NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
```

### `LoginResponse.java`
📁 [LoginResponse.java](../src/main/java/com/taskmanagement/dto/auth/LoginResponse.java)
```java
@Data @AllArgsConstructor @NoArgsConstructor
public class LoginResponse {
    private String token;
}
```

### `RegisterRequest.java`
📁 [RegisterRequest.java](../src/main/java/com/taskmanagement/dto/auth/RegisterRequest.java)
```java
@Data @AllArgsConstructor @NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "username is not empty")
    private String username;

    @NotBlank(message = "password is not empty")
    @Size(min = 6, max = 16, message = "password must be between 6 and 16 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,16}$", message = "password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "full name is not empty")
    private String fullName;

    @NotBlank(message = "role is not empty")
    private String role;
}
```

---
## 8. Test ở Postmain
###  Test Đăng ký `/api/auth/register
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/auth/login`
- **Body**:
```json
{
  "username": "admin123",
  "password": "Password123@",
  "fullName": "Nguyen Van A",
  "role": "ADMIN"
}

```
![Postmain-register-api](./images/register-api.png)

###  Test Đăng nhập `/api/auth/login
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/auth/login`
- **Body**:
```json
{
  "username": "admin123",
  "password": "Password123@"
}

```
![Postmain-login-api](./images/login-api.png)
## ✅ Kết quả đạt được
- [x] Tạo API `/register` và `/login`.
- [x] Mã hóa mật khẩu với `BCryptPasswordEncoder`.
- [x] Sinh và xác thực token JWT.
- [x] Bảo vệ endpoint với token.

---

## 9. Cấu hình `application.yml`
📁 [application.yml](../src/main/resources/application.yml)
```yaml
spring:
  application:
    name: task-management-internship

  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

  jackson:
    serialization:
      fail-on-empty-beans: false

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

springdoc:
  webjars:
    enabled: false

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
```

👉 Bạn cần tạo biến môi trường `.env` hoặc cấu hình trong IDE:

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/task_management
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000 # 1 ngày (milliseconds)
```

---

## ✅ Tổng kết

- [x] Cấu hình đăng ký/đăng nhập với mã hóa và JWT.
- [x] Tích hợp Swagger với Bearer Token.
- [x] Cấu hình `application.yml` tách biệt qua biến môi trường.
- [x] Dễ dàng test API bằng Swagger UI hoặc Postman.

---

