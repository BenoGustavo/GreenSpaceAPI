package com.greenspace.api.models;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.greenspace.api.enums.PermissionLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user", indexes = {
        @Index(name = "idx_user_profile", columnList = "profileID"),
        @Index(name = "idx_user_address", columnList = "addressID")
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

    @ManyToOne
    @JoinColumn(name = "profileID")
    private ProfileModel profile;

    @ManyToOne
    @JoinColumn(name = "addressID")
    private AddressModel address;
}