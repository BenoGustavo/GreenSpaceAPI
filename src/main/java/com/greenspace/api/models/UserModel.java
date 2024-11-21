package com.greenspace.api.models;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user", indexes = {
        @Index(name = "idx_user_profile", columnList = "profile_id"),
        @Index(name = "idx_user_address", columnList = "address_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID Id;

    @Column(unique = true)
    private String username;

    private String nickname;

    @JsonIgnore
    private String password;

    private String phoneNumber;
    @Column(unique = true)
    private String emailAddress;

    private Boolean isOnline;
    private Boolean isEmailValidated;
    private Boolean isDeactivated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id")
    private PermissionModel permissionLevel;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    private Timestamp loggedInAt;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id")
    private ProfileModel profile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private AddressModel address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ban_id", nullable = true)
    private BannedUsersModel ban;
}