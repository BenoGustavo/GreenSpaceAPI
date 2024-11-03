package com.greenspace.api.models;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.greenspace.api.enums.PermissionLevel;

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
    private String password;
    private String phoneNumber;

    @Column(unique = true)
    private String emailAddress;

    private Boolean isOnline;
    private Boolean isEmailValidated;
    private Boolean isDeactivated;
    private Boolean isBanned;
    private PermissionLevel permissionLevel;

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
}