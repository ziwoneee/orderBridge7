package com.itwillbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 기존 XML 수정 없이 pack_qty 단건 조회 전용 Mapper */
@Mapper
public interface SupplierPackRuleMapper {

    @Select({
        "SELECT COALESCE(",
        "  NULLIF(MAX(si.pack_qty), 0),",
        "  NULLIF(MAX(si.order_multiple), 0),",
        "  NULLIF(MAX(si.min_order_qty), 0),",
        "  NULLIF(MAX(si.conv_to_base), 0),",
        "  1",
        ") AS pack_qty",
        "FROM supplier_item si",
        "WHERE si.material_id = #{materialId}",
        "  AND (si.supply_available = 'Y' OR si.supply_available IS NULL)"
    })
    Double getPackQtyByMaterial(@Param("materialId") String materialId);
}
