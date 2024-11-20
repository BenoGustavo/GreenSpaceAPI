package com.greenspace.api.dto.banned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanUsersDTO {
    private String userEmailAddress;
    private String reason;

    // bannedUntil Timestamp fields
    private Integer year;
    private Integer month;
    private Integer dayOfMonth;
    private Integer hour;
    private Integer minute;
}
