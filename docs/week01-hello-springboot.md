# 🧪 Tuần 1 – Ôn tập Java & Làm quen Spring Boot

## 🎯 Mục tiêu
- Ôn tập kiến thức nền tảng Java: OOP, Collections, và Exception Handling.
- Cài đặt đầy đủ môi trường lập trình.
- Tạo ứng dụng Spring Boot cơ bản với cấu trúc chuẩn.
- Viết REST API đầu tiên: "Hello World".
- Hiểu mô hình MVC trong Spring Boot.

---

## 1. Ôn tập Java

### 🔹 OOP – Lập trình hướng đối tượng
- Sử dụng class, object, kế thừa, đa hình, đóng gói.
- Ví dụ: Tạo class `User` với thuộc tính `id`, `name`, `email`.

### 🔹 Java Collections
- Dùng `List`, `Map`, `Set` để lưu trữ dữ liệu linh hoạt.
- Áp dụng `ArrayList<User>` để quản lý danh sách người dùng.

### 🔹 Exception Handling
- Sử dụng `try-catch-finally` để xử lý ngoại lệ.
- Ví dụ: chia cho 0, truy cập mảng sai chỉ số.
- Tạo custom exception đơn giản: `UserNotFoundException`.

---

## 2. Cài đặt công cụ

| Phần mềm | Ghi chú |
|----------|--------|
| Java JDK 17+ | Biên dịch và chạy Spring Boot |
| Spring Boot CLI hoặc Spring Initializr | Khởi tạo project |
| Maven (hoặc Gradle) | Quản lý dependency |
| IntelliJ IDEA / Eclipse | IDE viết code |
| Git | Quản lý phiên bản |
| Postman | Kiểm thử REST API |

---

## 3. Tạo project Spring Boot cơ bản

### ✅ Dùng [https://start.spring.io/](https://start.spring.io/)
- **Group**: `com.sakurahino`
- **Artifact**: `task-management-internship`
- **Dependencies**:
    - Spring Web

### 🗂 Cấu trúc thư mục:
> Dưới đây là ảnh chụp cấu trúc thư mục thực tế trong IntelliJ IDEA sau khi khởi tạo project:

![Cấu trúc thư mục](./images/project-structure-week01.png)

---

## 4. Viết REST API đầu tiên – “Hello World”

### 📄 HelloController.java

```java
package com.sakurahino.taskmanagementinternship.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello-world")
    public String sayHello() {
        return "Hello World";
    }
}
```

### ✅ Kiểm thử bằng Postman

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/hello`
- **Response**:
```json
"Hello World!"
```

> Ảnh minh họa test API bằng Postman:

![Hello API Postman](./images/hello-api-postman.png)

---

## 5. Tìm hiểu mô hình MVC trong Spring Boot

| Thành phần | Vai trò | Trong tuần này |
|------------|--------|----------------|
| Model | Chứa dữ liệu, entity, DTO | (Chưa dùng – để dành tuần 2) |
| View | Giao diện (HTML, Thymeleaf...) | Không sử dụng |
| Controller | Xử lý request/response | `HelloController.java` |

> Trong Spring Boot, REST Controller là nơi nhận HTTP request và trả JSON response – tách biệt rõ ràng với tầng model và view.

---

## 6. Kết quả đạt được

- ✅ Cài đặt thành công môi trường phát triển Java + Spring Boot.
- ✅ Tạo được ứng dụng Spring Boot đầu tiên.
- ✅ Hiểu và sử dụng cấu trúc thư mục chuẩn của Spring Boot.
- ✅ Viết được REST API cơ bản (`GET /api/hello`).
- ✅ Làm quen với cách tổ chức mã nguồn theo mô hình MVC.

---

## 7. Ghi chú thêm

- File cấu hình chính `application.yml` để cấu hình port, context-path, v.v:
```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

- Lệnh chạy bằng Maven:
```bash
mvn spring-boot:run
```

- Có thể build file `.jar` bằng:
```bash
mvn clean package
java -jar target/task-management-internship-0.0.1-SNAPSHOT.jar
```

---

## 8. Tự đánh giá – Checklist Tuần 1

| Nội dung | Trạng thái |
|---------|------------|
| Ôn lại OOP, Collection, Exception | ✅ |
| Cài đặt môi trường | ✅ |
| Tạo project Spring Boot cơ bản | ✅ |
| Viết API Hello World | ✅ |
| Hiểu mô hình MVC | ✅ |
