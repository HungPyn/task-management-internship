
#  Tuần 5 – Quản lý Người dùng & Phân quyền

##  Mục tiêu
- Phân loại `Role`: `USER`, `ADMIN`.
- `ADMIN` có quyền xem danh sách người dùng và tất cả task.
- `USER` chỉ được xem/sửa task của chính mình.
- Tạo API quản lý người dùng dành riêng cho `ADMIN`.
- Áp dụng kiểm tra quyền bằng Spring Security.

---

## 1. Enum Role – `Role.java`
📁 [Role.java](../src/main/java/com/taskmanagement/enums/Role.java)
```java
public enum Role {
    ADMIN, USER;

    public static Role fromString(String role) {
        if (role == null) throw new IllegalArgumentException("Role cannot be null");
        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> ADMIN;
            case "USER" -> USER;
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        };
    }
}
```

---

## 2. DTO – `UserRequestDTO.java` & `UserResponseDTO.java`

📁 [UserRequestDTO.java](../src/main/java/com/taskmanagement/dto/user/UserRequestDTO.java)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "username is not empty")
    private String username;

    @NotBlank(message = "password is not empty")
    @Size(min = 6, max = 16, message = "password must be between 6 and 16 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,16}$",
             message = "password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "full name is not empty")
    private String fullName;

    @NotBlank(message = "role is not empty")
    private String role;
}
```

📁 [UserResponseDTO.java](../src/main/java/com/taskmanagement/dto/user/UserResponseDTO.java)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private String id;
    private String username;
    private String fullName;
    private Role role;
    private Instant created;
}
```

---

## 3. Repository – `UserRepository.java`
📁 [UserRepository.java](../src/main/java/com/taskmanagement/repository/UserRepository.java)
```java
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Page<User> findAllByOrderByCreatedDesc(Pageable pageable);
}
```

---

## 4. Mapper – `UserMapper.java`
📁 [UserMapper.java](../src/main/java/com/taskmanagement/mapper/UserMapper.java)
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toUserResponseDTO(User user);
}
```

---

## 5. Cấu hình bảo vệ endpoint theo Role – `SecurityConfig.java`
📁 [SecurityConfig.java](../src/main/java/com/taskmanagement/config/SecurityConfig.java)
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf().disable()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/api/user/**").hasAuthority("USER")
            .anyRequest().authenticated())
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

---
## 6. Test trên postmain
###  Test get all users by admin `/api/admin/users
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/admin/users`
- **Request body**:
![Get All User](./images/admin-get-users.png)
---

###  Test get all tasks by admin `/api/admin/task
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/admin/tasks`
- **Request body**:
  ![Get All User](./images/get-task-by-admin.png)
---

## ✅ Tổng kết
- [x] Tạo enum Role để phân quyền.
- [x] Xây dựng DTO cho user.
- [x] Repository hỗ trợ tìm kiếm & phân trang người dùng.
- [x] Mapper chuyển đổi từ entity sang DTO.
- [x] Áp dụng phân quyền cho route bằng Spring Security.
