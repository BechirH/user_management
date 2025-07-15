package com.hsurvey.userservice.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "organization-service", 
    url = "${organization.service.url}",
    configuration = com.hsurvey.userservice.config.FeignConfig.class
)
public interface OrganizationClient {
    @GetMapping("/api/organizations/{organizationId}/exists")
    ResponseEntity<Boolean> organizationExists(@PathVariable("organizationId") UUID organizationId);
}