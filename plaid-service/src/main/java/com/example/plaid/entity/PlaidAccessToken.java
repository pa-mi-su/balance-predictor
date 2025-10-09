package com.example.plaid.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "plaid_access_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uq_plaid_access_tokens_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_plaid_access_tokens_item", columnList = "item_id")
)
public class PlaidAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="access_token", nullable=false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name="item_id", nullable=false, columnDefinition = "TEXT")
    private String itemId;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getAccessToken() { return accessToken; }
    public String getItemId() { return itemId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
