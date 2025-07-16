
# Quản lý Task – RESTful API (Tuần 4)

## 📌 Mục tiêu
- Tạo đầy đủ API CRUD cho Task:
    - Get all tasks
    - Get task by ID
    - Create task
    - Update task
    - Delete task
- Phân quyền người dùng:
    - Người dùng chỉ xem/sửa được task của chính mình.
- Tích hợp Swagger UI để test API.

---

## 1. Controller – Admin (`TaskAdminController.java`)
📁 [TaskAdminController.java](../src/main/java/com/taskmanagement/controller/admin/TaskAdminController.java)
```java
@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class TaskAdminController {

    private final TaskService taskService;

  @GetMapping
  public SuccessResponse getTasks(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(required = false) Status status,
          @RequestParam(defaultValue = "created") String sortBy,
          @RequestParam(defaultValue = "desc") String direction
  ) {
    var result = taskService.getTasks(page, size, status, sortBy, direction);
    return new SuccessResponse(result);
  }

    @PostMapping
    public SuccessResponse createTask(@RequestBody @Valid TaskRequestDTO dto) {
        var response = taskService.create(dto);
        return new SuccessResponse(response);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateTask(@PathVariable String id,@RequestBody @Valid TaskRequestDTO dto) {
        var response = taskService.update(id,dto);
        return new SuccessResponse(response);
    }

    @DeleteMapping("/{id}")
    public SuccessResponse deleteTask(@PathVariable String id) {
        taskService.delete(id);
        return new SuccessResponse();
    }
}
```

---

## 2. Controller – User (`CurrentTaskController.java`)
📁 [CurrentTaskController.java](../src/main/java/com/taskmanagement/controller/user/CurrentTaskController.java)
```java
@RestController
@RequestMapping("/api/user/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class CurrentTaskController {

    private final TaskService taskService;

  @GetMapping
  public SuccessResponse getTasks(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(required = false) Status status,
          @RequestParam(defaultValue = "created") String sortBy,
          @RequestParam(defaultValue = "desc") String direction
  ) {
    var result = taskService.getTasks(page, size, status, sortBy, direction);
    return new SuccessResponse(result);
  }

    @PostMapping
    public SuccessResponse createTask(@RequestBody @Valid TaskRequestDTO dto) {
        var response = taskService.create(dto);
        return new SuccessResponse(response);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateTask(@PathVariable String id,@RequestBody @Valid TaskRequestDTO dto) {
        var response = taskService.update(id,dto);
        return new SuccessResponse(response);
    }
}
```

---

## 3. DTOs – Task

### `TaskRequestDTO.java`
📁 [TaskRequestDTO.java](../src/main/java/com/taskmanagement/dto/task/TaskRequestDTO.java)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    @Pattern(regexp = "TODO|IN_PROGRESS|COMPLETED|CANCELLED")
    private String status;

    @NotNull private Instant created;
    @NotNull private Instant updated;
    @NotNull private Instant deadline;

    private String userId;
}
```

### `TaskResponseDTO.java`
📁 [TaskResponseDTO.java](../src/main/java/com/taskmanagement/dto/task/TaskResponseDTO.java)
```java
@Data
@NoArgsConstructor
public class TaskResponseDTO {
    private String id;
    private String title;
    private String description;
    private Status status;
    private Instant created;
    private Instant updated;
    private Instant deadline;
    private String username;
}
```

---

## 4. Phân quyền với `AuthHelper.java`
📁 [AuthHelper.java](../src/main/java/com/taskmanagement/security/AuthHelper.java)
```java
@Component
@Slf4j
public class AuthHelper {
    public String getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }

    public CustomUserDetail getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      ...
        return (CustomUserDetail) principal;
    }

    private boolean hasRole(String role) {
        return getCurrentUser().getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}
```

---

## 5. Phân trang – `PaginatedResponse.java`
📁 [PaginatedResponse.java](../src/main/java/com/taskmanagement/dto/PaginatedResponse.java)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> items;
    private int page;
    private int totalItems;
    private int totalPages;
    private boolean hasNext;
}
```

---

## 6. Swagger UI cấu hình
📁 [OpenApiConfig.java](../src/main/java/com/taskmanagement/config/OpenApiConfig.java)
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
            .info(new Info().title("Task Management API").version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList(scheme))
            .components(new Components()
                .addSecuritySchemes(scheme,
                    new SecurityScheme().name(scheme).type(SecurityScheme.Type.HTTP)
                        .scheme("bearer").bearerFormat("JWT")));
    }
}
```

---

## ✅ Kết quả đạt được
- [x] Tạo đầy đủ API CRUD cho Task.
- [x] Áp dụng phân quyền bằng `@PreAuthorize`.
- [x] Áp dụng Swagger để test.
- [x] Phân trang, lọc và sắp xếp.

### Test trên Posmain:
###  Test create bằng user thành công `/api/user/tasks
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/user/tasks`
- **Request body**:
```json
{
  "title": "Test Api create task",
  "description": "thêm thành công task user",
  "status": "PENDING",
  "created": "2025-07-16T17:18:46.652Z",
  "updated": "2025-07-16T17:18:46.652Z",
  "deadline": "2025-07-16T17:18:46.652Z",
  "userId": "12e44664-bbad-429c-a046-57c10eb56d58"
}

```
![Create by User](./images/create-task-by-user.png)

---
###  Test create bằng user khi create bằng user của người khác `/api/user/tasks
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/user/tasks`
- **Request body**:
```json
{
  "title": "Test Api create task",
  "description": "thêm thành công task user",
  "status": "PENDING",
  "created": "2025-07-16T17:18:46.652Z",
  "updated": "2025-07-16T17:18:46.652Z",
  "deadline": "2025-07-16T17:18:46.652Z",
  "userId": "30c7486b-f21d-4e89-8d05-5dda4cbcd007"
}

```
![Create by User](./images/create-fail.png)

---
###  Test get all task của user đăng nhập đã phân trang `  /api/user/tasks
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/user/tasks`

![Create by User](./images/get-task-by-user.png)

---