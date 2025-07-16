#  Tuần 10 – Tổng kết & Thuyết trình Dự Án

##  Mục tiêu

* Trình bày lại toàn bộ quá trình xây dựng hệ thống.
* Giải thích kiến trúc, thiết kế API, database, và chức năng chính.
* Thể hiện kỹ năng clean code, bảo mật và quản lý dự án.

---

## 1.  Mô tả Dự án

**Tên:** Task Management API
**Mô tả:** Hệ thống quản lý công việc theo role (USER/ADMIN), hỗ trợ đăng ký, đăng nhập, phân quyền, phân trang, lọc, sắp xếp task.

---

## 2. Kiến trúc Hệ thống

```
Client (Postman/Swagger)
        |
Spring Boot REST API
        |
Service -> Repository -> Database (MySQL)
```

### Thành phần chính:

* **Spring Boot**: Framework chính.
* **Spring Security + JWT**: Xác thực & phân quyền.
* **Swagger UI**: Test API dễ dàng.
* **MySQL**: Lưu trữ dữ liệu.
* **DTO + Mapper**: Chuẩn hoá dữ liệu trả về.
* **Exception Handling**: Phản hồi lỗi chuẩn.

---

## 3. Chức năng đã triển khai

| Chức năng                 | Đã hoàn thành |
| ------------------------- | ------------- |
| Đăng ký / Đăng nhập       | ✅             |
| Sinh & xác thực JWT Token | ✅             |
| Phân quyền ADMIN / USER   | ✅             |
| CRUD Task (Admin/User)    | ✅             |
| Phân trang, lọc, sắp xếp  | ✅             |
| Swagger UI test API       | ✅             |
| Global Exception Handler  | ✅             |
| Clean code + Logging      | ✅             |

---

## 4. Luồng API chính

* `POST /api/auth/register` – Đăng ký
* `POST /api/auth/login` – Đăng nhập (trả JWT token)
* `GET /api/admin/tasks` – Admin lấy tất cả task
* `GET /api/user/tasks` – USER chỉ xem task của chính mình
* `POST /api/admin/users` – Admin tạo user
* ...

---

## 5. Mô hình Dữ liệu (Entity)

###  User

```java
class User {
    String id;
    String username;
    String fullName;
    String password;
    Role role; // ADMIN, USER
    Instant created;
}
```

###  Task

```java
class Task {
    String id;
    String title;
    String description;
    Status status; // PENDING, IN_PROGRESS, COMPLETED
    Instant created;
    Instant updated;
    Instant deadline;
    User user; // quan hệ @ManyToOne
}
```

---

## 6. Swagger UI

> Đường dẫn: `http://localhost:8080/swagger-ui/index.html`

* ✅ Có cấu hình Bearer Token
* ✅ Dễ test API (giao diện trực quan)
* ✅ Tự động sinh docs từ controller

---

## 7. Clean Code & Logging

* Sử dụng `SLF4J Logger` với `@Slf4j`.
* Cấu trúc chuẩn `controller → service → repository`.
* DTO tách biệt với Entity.
* Exception rõ ràng với `AppException`, `ExceptionCode`.
* Response chuẩn hoá qua `ApiResponse`, `ErrorResponse`.

---

## 8. Bảo mật

* Spring Security + JWT (Stateless)
* Filter kiểm tra token.
* Phân quyền theo role.
* Không lộ password (hash bằng `BCrypt`).

---

## 9. Demo

* ✅ Đăng ký + Đăng nhập
* ✅ Admin xem danh sách task
* ✅ User xem task của mình
* ✅ Phân trang + lọc + sort task
* ✅ Swagger UI + Postman test

---

## 10. Đánh giá

| Tiêu chí             | Mức độ |
| -------------------- | ------ |
| Clean code           | ⭐⭐⭐⭐   |
| Bảo mật (JWT, role)  | ⭐⭐⭐⭐   |
| Hiệu năng (paging)   | ⭐⭐⭐    |
| Swagger UI           | ⭐⭐⭐⭐   |
| Validation + Logging | ⭐⭐⭐⭐   |

---

>  **Tổng kết:** Dự án đã hoàn chỉnh backend REST API chuẩn RESTful, bảo mật JWT, phân quyền, ghi log, phản hồi API chuẩn, dễ test với Swagger UI.
