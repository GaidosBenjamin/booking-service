package com.bgaidos.booking.tier;

import com.bgaidos.booking.api.tier.TierCreateRequest;
import com.bgaidos.booking.api.tier.TierPatchRequest;
import com.bgaidos.booking.api.tier.TierResponse;
import com.bgaidos.booking.entity.Tier;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Tier toEntity(TierCreateRequest request);

    default TierResponse toResponse(Tier tier, boolean memberDiscount) {
        return new TierResponse(
            tier.getId(),
            tier.getName(),
            tier.getDescription(),
            tier.getBasePrice(),
            tier.getDiscountPrice(),
            tier.getCurrency(),
            memberDiscount,
            tier.getCreatedOn());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void applyPatch(TierPatchRequest request, @MappingTarget Tier tier);
}
