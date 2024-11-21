package com.greenspace.api.dto.banned;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanRevokeDTO {
    private String userEmailAddress;
    private String reason;
}
