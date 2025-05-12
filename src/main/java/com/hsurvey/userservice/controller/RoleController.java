package com.hsurvey.userservice.controller;


import com.hsurvey.userservice.dto.RoleDTO;
import com.hsurvey.userservice.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public RoleDTO createRole(@RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }
}