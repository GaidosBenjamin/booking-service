package com.bgaidos.booking.building;

import com.bgaidos.booking.api.building.BuildingCreateRequest;
import com.bgaidos.booking.api.building.BuildingPatchRequest;
import com.bgaidos.booking.api.building.BuildingResponse;
import com.bgaidos.booking.api.tier.TierResponse;
import com.bgaidos.booking.entity.Building;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BuildingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Building toEntity(BuildingCreateRequest request);

    @Mapping(target = "id", source = "building.id")
    @Mapping(target = "name", source = "building.name")
    @Mapping(target = "description", source = "building.description")
    @Mapping(target = "highlights", source = "building.highlights")
    @Mapping(target = "imageUrl", source = "building.imageUrl")
    @Mapping(target = "createdOn", source = "building.createdOn")
    @Mapping(target = "tier", source = "tier")
    @Mapping(target = "isFull", source = "isFull")
    BuildingResponse toResponse(Building building, TierResponse tier, boolean isFull);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void applyPatch(BuildingPatchRequest request, @MappingTarget Building building);
}
