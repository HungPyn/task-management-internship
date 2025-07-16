# Tuần 7 – Pagination, Sorting, Filtering

## 🎯 Mục tiêu
- Áp dụng phân trang cho danh sách task.
- Thêm filter theo trạng thái (`PENDING`, `IN_PROGRESS`, `COMPLETED`).
- Sort theo ngày tạo (`created`), deadline (`deadline`).

---

## 1. API – TaskAdminController & CurrentTaskController
📁 [TaskAdminController.java](../src/main/java/com/taskmanagement/controller/admin/TaskAdminController.java)
```java
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
```
📁 [CurrentTaskController.java](../src/main/java/com/taskmanagement/controller/user/CurrentTaskController.java)
```java
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
```
---
## 2. Enum – Status.java
📁 [Status.java](../src/main/java/com/taskmanagement/enums/Status.java)
```java
public enum Status {
PENDING,
IN_PROGRESS,
COMPLETED;

    public static Status fromString(String status) {
        if (status == null) throw new IllegalArgumentException("Status cannot be null");
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> Status.PENDING;
            case "IN_PROGRESS" -> Status.IN_PROGRESS;
            case "COMPLETED" -> Status.COMPLETED;
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };
    }
}
```
---
## 3. DTO – PaginatedResponse.java
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
## 5. Service
📁 [TaskServiceImpl.java](../src/main/java/com/taskmanagement/service/impl/TaskServiceImpl.java)
```java
    @Override
    public PaginatedResponse<TaskResponseDTO> getTasks(int page, int size, Status status, String sortBy, String direction) {
        if (authHelper.isAdmin()) {
            return getTasksForAdmin(page, size, status, sortBy, direction);
        } else {
            return getTasksForUser(page, size, status, sortBy, direction);
        }
    }

    private PaginatedResponse<TaskResponseDTO> getTasksForAdmin(int page, int size, Status status, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Task> taskPage = (status != null)
                ? taskRepository.findByStatus(status, pageable)
                : taskRepository.findAll(pageable);
        return toPaginatedResponse(taskPage, "ADMIN");
    }

    private PaginatedResponse<TaskResponseDTO> getTasksForUser(int page, int size, Status status, String sortBy, String direction) {
        String currentUserId = authHelper.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Task> taskPage = (status != null)
                ? taskRepository.findByUserIdAndStatus(currentUserId, status, pageable)
                : taskRepository.findByUserId(currentUserId, pageable);
        return toPaginatedResponse(taskPage, currentUserId.toString());
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        List<String> allowedSortFields = List.of("created", "deadline");
        if (!allowedSortFields.contains(sortBy)) {
            log.warn("Invalid sort field '{}'. Defaulting to 'created'.", sortBy);
            sortBy = "created";
        }

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private PaginatedResponse<TaskResponseDTO> toPaginatedResponse(Page<Task> taskPage, String ownerIdentifier) {
        List<TaskResponseDTO> dtoList = taskPage.getContent().stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();

        log.info("Fetched tasks for '{}': page={}, totalElements={}, totalPages={}",
                ownerIdentifier, taskPage.getNumber(), taskPage.getTotalElements(), taskPage.getTotalPages());

        return new PaginatedResponse<>(
                dtoList,
                taskPage.getNumber(),
                (int) taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.hasNext()
        );
    }
```
## 6. Ảnh test trên postmain
- **Method**: `GET`
- **URL**: http://localhost:8080/api/admin/tasks?page=0&size=5&status=PENDING&sortBy=deadline&direction=desc`

![Postmain-register-api](./images/test-week07.png)
---
## ✅ Kết quả đạt được
- [x] Áp dụng phân trang cho danh sách task.

- [x] Thêm filter theo trạng thái (PENDING, IN_PROGRESS, COMPLETED).

- [x] Sort theo ngày tạo (created), deadline (deadline).

