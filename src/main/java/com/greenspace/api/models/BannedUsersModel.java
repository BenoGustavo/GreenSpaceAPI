package com.greenspace.api.models;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_banned_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannedUsersModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserModel user;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banned_by_id")
    private UserModel bannedBy;
    @Column(nullable = false, updatable = false)
    private String reason;

    @CreationTimestamp
    private Timestamp bannedAt;
    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp bannedUntil;
}
