#  Tuần 6 – Logging, Exception Handling, Validation

##  Mục tiêu
- Tạo `custom exception` và `global exception handler`.
- Sử dụng `@Valid` để validate DTO.
- Ghi log với `SLF4J` & `Logger`.
- Gửi phản hồi API chuẩn (`ApiResponse`, `ErrorResponse`, `SuccessResponse`).

---
## 1. ✅ Validation với @Valid
📁 [RegisterRequest.java](../src/main/java/com/taskmanagement/dto/auth/RegisterRequest.java)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "username is not empty")
    private String username;

    @NotBlank(message = "password is not empty")
    @Size(min = 6, max = 16, message = "password must be between 6 and 16 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,16}$",
            message = "password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "full name is not empty")
    private String fullName;

    @NotBlank(message = "role is not empty")
    private String role;
}
```
📁 [TaskRequestDTO.java](../src/main/java/com/taskmanagement/dto/task/TaskRequestDTO.java)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @NotBlank(message = "Status must not be blank")
    @Pattern(
            regexp = "PENDING|IN_PROGRESS|COMPLETED",
            message = "Invalid status value"
    )
    private String status;

    @NotNull(message = "Created time must not be null")
    private Instant created;

    private Instant updated;

    @NotNull(message = "Deadline must not be null")
    private Instant deadline;

    private String userId;
}
```
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
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,16}$",
            message = "password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "full name is not empty")
    private String fullName;

    @NotBlank(message = "role is not empty")
    private String role;
}
```
---
## 2. Controller kích hoạt @valid

📁 [AdminUserController.java](../src/main/java/com/taskmanagement/controller/admin/AdminUserController.java)
```java
   @PostMapping
public SuccessResponse createUser(@RequestBody @Valid UserRequestDTO dto) {
    var response = userService.create(dto);
    return new SuccessResponse(response);
}

@PutMapping("/{id}")
public SuccessResponse updateUser(
        @PathVariable String id,
        @RequestBody @Valid UserRequestDTO dto) {
    var response = userService.update(id, dto);
    return new SuccessResponse(response);
}
```
📁 [TaskAdminController.java](../src/main/java/com/taskmanagement/controller/admin/TaskAdminController.java)
```java
    @PostMapping
public SuccessResponse createTask(@RequestBody @Valid TaskRequestDTO dto) {
    var response =taskService.create(dto);
    return new SuccessResponse(response);
}

@PutMapping("/{id}")
public SuccessResponse updateTask(@PathVariable String id,@RequestBody @Valid TaskRequestDTO dto) {
    var response = taskService.update(id,dto);
    return new SuccessResponse(response);
}
```
📁 [CurrentTaskController.java](../src/main/java/com/taskmanagement/controller/user/CurrentTaskController.java)
```java
@PostMapping
public SuccessResponse createTask(@RequestBody @Valid TaskRequestDTO dto) {
    var response =taskService.create(dto);
    return new SuccessResponse(response);
}

@PutMapping("/{id}")
public SuccessResponse updateTask(@PathVariable String id,@RequestBody @Valid TaskRequestDTO dto) {
    var response = taskService.update(id,dto);
    return new SuccessResponse(response);
}
```
📁 [CurrentUserController.java](../src/main/java/com/taskmanagement/controller/user/CurrentUserController.java)
```java
    @PutMapping("/{id}")
public SuccessResponse updateUser(
        @PathVariable String id,
        @RequestBody @Valid UserRequestDTO dto) {
    var response = userService.update(id, dto);
    return new SuccessResponse(response);
}
```
# 2. Custom Exception
📁 [ResourceException.java](../src/main/java/com/taskmanagement/ex/ResourceException.java)
```java
@Getter
public class ResourceException extends RuntimeException{

    private final int status;
    private final String error;

    public ResourceException(int status, String error) {
        super(error);
        this.status = status;
        this.error = error;
    }
}

```
📁 [AppException.java](../src/main/java/com/taskmanagement/ex/AppException.java)
```java
@Getter
public class AppException extends ResourceException {
    private final ExceptionCode code;

    public AppException(ExceptionCode code) {
        super(code.getStatus(), code.getError());
        this.code = code;
    }

    public AppException(ExceptionCode code, Object... args) {
        super(code.getStatus(), String.format(code.getError(), args));
        this.code = code;
    }

}
```
📁 [ExceptionCode.java](../src/main/java/com/taskmanagement/ex/ExceptionCode.java)
```java
@Getter
public enum ExceptionCode {
    USER_NOT_FOUND(1001, "User not found"),
    PASSWORD_INCORRECT(1002, "Password incorrect"),
    USER_FOUND(1003, "Users have existed"),
    UNAUTHORIZED(2003, "Unauthorized"),
    USER_ID_REQUIRED(3001,"User ID is required."),
    TASK_NOT_FOUND(3002,"Task not found");

    private final int status;
    private final String error;

    ExceptionCode(int status, String error) {
        this.status = status;
        this.error = error;
    }
}

```
---
## 3. Global Exception Handling
📁 [BaseExceptionHandler.java](../src/main/java/com/taskmanagement/retresponse/BaseExceptionHandler.java)
```java
@Slf4j
public abstract class BaseExceptionHandler {

    public ApiResponse handleResourceException(ResourceException e) {
        log.error("Lỗi nghiệp vụ: {}", e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(e.getStatus(), e.getError(), headers);
    }

    public ErrorResponse handleValidationError(MethodArgumentNotValidException e) {
        log.error("Lỗi validate: {}", e.getBindingResult().getFieldErrors());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ErrorResponse(e.getBindingResult().getFieldErrors(), headers);
    }

    public ApiResponse handleAllException(Exception e) {
        log.error("Lỗi logic code: {}", e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(500, "Lỗi hệ thống. Vui lòng thử lại sau.", headers);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse handleEnumConversion(MethodArgumentTypeMismatchException e) {
        log.error("Invalid enum value for parameter '{}': {}", e.getName(), e.getValue());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ApiResponse(400, "Invalid status", headers);
    }
}

```
📁 [RestExceptionHandler.java](../src/main/java/com/taskmanagement/retresponse/RestExceptionHandler.java)
```java
@RestControllerAdvice
public class RestExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ResourceException.class)
    public ApiResponse handleResource(ResourceException e) {
        return super.handleResourceException(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        return super.handleValidationError(e);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse handleOther(Exception e) {
        return super.handleAllException(e);
    }
}

```
---
### 4. Chuẩn hóa phản hồi AP
📁 [ApiResponse.java](../src/main/java/com/taskmanagement/retresponse/ApiResponse.java)
```java
public class ApiResponse extends ResponseEntity<ApiResponse.Payload> {

    public ApiResponse(HttpStatus status) {
        super(new Payload(status.value(), status.getReasonPhrase(), null), status);
    }

    public ApiResponse(HttpStatus status, Object data) {
        super(new Payload(status.value(), status.getReasonPhrase(), data), status);
    }

    public ApiResponse(int status, String message, HttpHeaders headers, Object data) {
        super(new Payload(status, message, data), headers, HttpStatus.OK);
    }

    public ApiResponse(int status, String message, HttpHeaders headers) {
        super(new Payload(status, message, null), headers, HttpStatus.OK);
    }

    @Value
    public static class Payload {
        int status;
        String error;
        Object data;
    }
}

```
📁 [SuccessResponse.java](../src/main/java/com/taskmanagement/retresponse/SuccessResponse.java)
```java
public class SuccessResponse extends ApiResponse {
    public SuccessResponse() {
        super(HttpStatus.OK);
    }

    public SuccessResponse(Object data) {
        super(HttpStatus.OK, data);
    }
}

```
📁 [ErrorResponse.java](../src/main/java/com/taskmanagement/retresponse/ErrorResponse.java)
```java
public class ErrorResponse extends ApiResponse {

    public ErrorResponse(List<FieldError> errors, HttpHeaders headers) {
        super(HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu không hợp lệ",
                headers,
                new ApiError(errors.stream()
                        .map(e -> new Error(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @Value
    public static class ApiError {
        List<Error> errors;
    }

    @Value
    public static class Error {
        String field;
        Object value;
        String message;
    }
}


```
---
## 5. Test ở Postmain
- **Method**: `Post`
- **URL**: `http://localhost:8080/api/hello`
- **Request body**:
```json
{
  "username": "hung123",
  "password": "123456a@",
  "fullName": "",
  "Role": null
}
```
- **Response**
```json
{
  "status": 400,
  "error": "Dữ liệu không hợp lệ",
  "data": {
    "errors": [
      {
        "field": "role",
        "value": null,
        "message": "role is not empty"
      },
      {
        "field": "fullName",
        "value": null,
        "message": "full name is not empty"
      }
    ]
  }
}
```

---
## ✅ Tổng kết
- [x] Áp dụng @Valid để validate dữ liệu đầu vào.

- [x] Xây dựng AppException và ExceptionCode chuẩn hóa mã lỗi.

- [x] Sử dụng @RestControllerAdvice xử lý lỗi toàn cục.

- [x] Ghi log chi tiết khi lỗi xảy ra với SLF4J.

- [x] Trả về phản hồi chuẩn bằng ApiResponse, SuccessResponse, ErrorResponse.