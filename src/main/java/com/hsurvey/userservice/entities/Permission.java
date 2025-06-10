package com.hsurvey.userservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;  // e.g., "USER_READ", "USER_CREATE"

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    private String description;
}