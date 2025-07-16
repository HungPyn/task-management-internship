package com.taskmanagement;

import com.taskmanagement.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class TaskManagementInternshipApplicationTests {

    @Test
    void contextLoads() {
    }
    @Bean
    public AuthService authService() {
        return Mockito.mock(AuthService.class);
    }
}
