
# 📅 Báo cáo Tuần 2: Làm việc với Database (MySQL) & JPA

## 🎯 Mục tiêu
- Cài đặt và kết nối cơ sở dữ liệu MySQL.
- Tạo entity `User` và `Task` bằng JPA.
- Cấu hình kết nối với database trong `application.yml`.
- Tạo repository, service và controller CRUD.
- Kiểm thử API bằng Postman.

---

## 1. Cài đặt và cấu hình MySQL

### 1.1. Tạo database

```sql
CREATE DATABASE task_management;
```

### 1.2. Cấu hình kết nối trong `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/task_management
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

---

## 2. Entity bằng JPA

### 2.1. Entity: `User`

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created")
    private Instant created;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @PrePersist
    private void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
```

### 2.2. Entity: `Task`

```java
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at")
    private Instant created;

    @Column(name = "updated_at")
    private Instant updated;

    @Column(name = "deadline")
    private Instant deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
```

---

## 3. Repository

```java
public interface UserRepository extends JpaRepository<User, String> {
}
```

```java
public interface TaskRepository extends JpaRepository<Task, String> {
}
```

---

## 4. Service Layer

### 4.1. TaskService

```java
public interface TaskService {
    Task create(Task task);
    Task update(String id, Task task);
    void delete(String id);
}
```

```java
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Task create(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task update(String id, Task taskDetails) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setDeadline(taskDetails.getDeadline());
        task.setUpdated(Instant.now());
        return taskRepository.save(task);
    }

    @Override
    public void delete(String id) {
        taskRepository.deleteById(id);
    }
}
```

### 4.2. UserService

```java
public interface UserService {
    User create(User user);
    User update(String id, User user);
    void delete(String id);
    List<User> getAll();
}
```

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(String id, User data) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(data.getFullName());
        user.setUsername(data.getUsername());
        user.setPassword(data.getPassword());
        user.setRole(data.getRole());
        return userRepository.save(user);
    }

    @Override
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
```

---

## 5. Controller

### 5.1. TaskController

```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.create(task);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable String id, @RequestBody Task task) {
        return taskService.update(id, task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        taskService.delete(id);
    }
}
```

### 5.2. UserController

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }
}
```

---
## 6. Test API bằng Postman

### 6.1. Task API

#### Tạo Task
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/tasks`
- **Response**:
```json
{
  "title": "Học Spring Boot",
  "description": "Làm bài thực hành tuần 2",
  "status": "PENDING",
  "deadline": "2025-07-20T17:00:00Z",
  "user": {
    "id": "user-uuid-123"
  }
}
```
#### Cập nhập task
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/tasks/{id}`
- **Response**:
```json
{
  "title": "Học Spring Boot nâng cao",
  "description": "Hoàn thiện ứng dụng Task Management",
  "status": "IN_PROGRESS",
  "deadline": "2025-07-22T10:00:00Z",
  "user": {
    "id": "user-uuid-123"
  }
}
```
#### Lấy danh sách Task
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/tasks`
- **Response**:
```json
[
  {
    "id": "1c3d-b5e4...",
    "title": "Học Spring Boot",
    "description": "Làm bài thực hành tuần 2",
    "status": "PENDING",
    "created": "2025-07-15T10:00:00Z",
    "updated": null,
    "deadline": "2025-07-20T17:00:00Z",
    "user": {
      "id": "user-uuid-123",
      "username": "admin"
    }
  }
]
```
#### Xóa task
- **Method**: `DELETE`
- **URL**: `http://localhost:8080/api/tasks/{id}`
- **Response**:
 (Không cần)

---

### 6.2. User API

#### Tạo user
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/users`
- **Response**:
```json
{
  "username": "admin",
  "password": "123456",
  "fullName": "Nguyễn Văn A",
  "role": "ADMIN"
}
```
#### Cập nhập user
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/users/{id}`
- **Response**:
```json
{
  "username": "admin123",
  "password": "654321",
  "fullName": "Nguyễn Văn B",
  "role": "USER"
}
```
#### Lấy danh sách user
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/users`
- **Response**:
```json
[
  {
    "id": "user-uuid-123",
    "username": "admin",
    "fullName": "Nguyễn Văn A",
    "role": "ADMIN",
    "created": "2025-07-15T09:00:00Z"
  }
]
```
#### Xóa user
- **Method**: `DELETE`
- **URL**: `http://localhost:8080/api/users/{id}`
- **Response**:
  (Không cần)



---
## ✅ Kết quả đạt được

- [x] Cài đặt MySQL, kết nối với Spring Boot.
- [x] Tạo Entity: `User` và `Task` với UUID và quan hệ 1-n.
- [x] Tạo Repository, Service, Controller cho `User` và `Task`.
- [x] Test thành công các API CRUD với Postman.

---
