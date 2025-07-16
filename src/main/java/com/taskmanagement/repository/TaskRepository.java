package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository  extends JpaRepository<Task, String> {

    Page<Task> findByUserId(String userId, Pageable pageable);
    Page<Task> findByUserIdAndStatus(String userId, Status status, Pageable pageable);
    Page<Task> findByStatus(Status status, Pageable pageable);

}
