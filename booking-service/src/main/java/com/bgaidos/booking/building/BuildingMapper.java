package com.bgaidos.booking.building;

import com.bgaidos.booking.api.building.BuildingCreateRequest;
import com.bgaidos.booking.api.building.BuildingPatchRequest;
import com.bgaidos.booking.api.building.BuildingResponse;
import com.bgaidos.booking.api.building.HighlightItemDto;
import com.bgaidos.booking.api.tier.TierResponse;
import com.bgaidos.booking.entity.Building;
import com.bgaidos.booking.entity.HighlightItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Mapping(target = "highlights", source = "highlights", qualifiedByName = "dtoToEntity")
    Building toEntity(BuildingCreateRequest request);

    @Mapping(target = "id", source = "building.id")
    @Mapping(target = "name", source = "building.name")
    @Mapping(target = "description", source = "building.description")
    @Mapping(target = "highlights", source = "building.highlights", qualifiedByName = "entityToDto")
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
    @Mapping(target = "highlights", source = "highlights", qualifiedByName = "dtoToEntity")
    void applyPatch(BuildingPatchRequest request, @MappingTarget Building building);

    @Named("entityToDto")
    default Map<String, List<HighlightItemDto>> entityToDto(Map<String, List<HighlightItem>> entity) {
        if (entity == null) return null;
        return entity.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().stream()
                .map(h -> new HighlightItemDto(h.icon(), h.text()))
                .toList()
        ));
    }

    @Named("dtoToEntity")
    default Map<String, List<HighlightItem>> dtoToEntity(Map<String, List<HighlightItemDto>> dto) {
        if (dto == null) return null;
        return dto.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().stream()
                .map(h -> new HighlightItem(h.icon(), h.text()))
                .toList()
        ));
    }
}
