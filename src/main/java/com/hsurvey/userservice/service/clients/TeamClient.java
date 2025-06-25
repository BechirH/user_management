package com.hsurvey.userservice.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "team-service", url = "${team.service.url}")
public interface TeamClient {
    @GetMapping("/api/teams/user/{userId}")
    ResponseEntity<UUID> getTeamIdByUserId(@PathVariable("userId") UUID userId);
} 