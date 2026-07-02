package com.campusconnect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拼团成员展示对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyMemberDTO {

    private Long userId;

    private String nickname;

    private String avatar;

    private String role;
}