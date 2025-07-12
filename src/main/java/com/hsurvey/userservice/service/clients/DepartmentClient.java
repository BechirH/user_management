package com.hsurvey.userservice.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "department-service", 
    url = "${department.service.url}",
    configuration = com.hsurvey.userservice.config.FeignConfig.class
)
public interface DepartmentClient {
    @GetMapping("/api/departments/user/{userId}")
    ResponseEntity<UUID> getDepartmentIdByUserId(@PathVariable("userId") UUID userId);
} 