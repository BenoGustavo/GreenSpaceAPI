package com.greenspace.api.features.user.banned;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenspace.api.models.BannedUsersModel;

public interface BannedUsersRepository extends JpaRepository<BannedUsersModel, UUID> {
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BannedUsersModel b WHERE b.user.emailAddress = :emailAddress")
    boolean existsByEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("SELECT b FROM BannedUsersModel b WHERE b.user.emailAddress = :emailAddress")
    Optional<BannedUsersModel> findByUserEmailAddress(String emailAddress);

    List<BannedUsersModel> findAllByBannedUntilBefore(Timestamp timestamp);
}
