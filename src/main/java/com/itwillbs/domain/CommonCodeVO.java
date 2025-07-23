package com.itwillbs.domain;

import lombok.Data;

@Data
public class CommonCodeVO {

    private String codeId;      // 코드 ID
    private String groupId;     // 그룹 ID
    private String codeName;    // 코드명
    private String description; // 코드 설명
    private int sortOrder;      // 정렬 순서
    private String useYn;       // 사용 여부 (Y/N)

}
