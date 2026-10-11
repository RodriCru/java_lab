package com.lrch.javalab.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Cuenta de usuario.
 *
 * <p>Se aplica borrado logico: la cuenta se marca con {@code active = false} y nunca se borra
 * fisicamente de la base de datos.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name")
    private String displayName;

    /** Milisegundos desde epoch: evita problemas de tipos fecha en SQLite. */
    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "last_login_at")
    private Long lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** El default en la columna permite que `ddl-auto: update` agregue el campo a una base con usuarios. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
    private UserRole role = UserRole.USER;

    protected User() {
    }

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return Instant.ofEpochMilli(createdAt);
    }

    public Long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isActive() {
        return active;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isSuperAdmin() {
        return role == UserRole.SUPER_ADMIN;
    }

    public void activate() {
        this.active = true;
    }

    /** Borrado logico. */
    public void deactivate() {
        this.active = false;
    }
}
