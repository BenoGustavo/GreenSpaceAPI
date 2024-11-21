package com.greenspace.api.models;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "tb_ban_revoke_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanRevokeLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, name = "id")
    private UUID id;
    private String bannedUserEmailAddress;
    @Column(nullable = false, updatable = false)
    private String reason;

    @CreationTimestamp
    private Timestamp revokedAt;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "revoked_by_id")
    private UserModel revokedBy;

    // Ban info
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banned_user_id")
    private UserModel bannedUser;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banned_by_id")
    private UserModel bannedBy;
    @Column(nullable = false, updatable = false)
    private String banReason;

    @CreationTimestamp
    private Timestamp bannedAt;
    private Timestamp bannedUntil;
}
