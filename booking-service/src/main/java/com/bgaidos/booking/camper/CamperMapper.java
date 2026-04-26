package com.bgaidos.booking.camper;

import com.bgaidos.booking.api.camper.CamperCreateRequest;
import com.bgaidos.booking.api.camper.CamperPatchRequest;
import com.bgaidos.booking.api.camper.CamperResponse;
import com.bgaidos.booking.api.camper.RoomHoldSummary;
import com.bgaidos.booking.entity.Camper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CamperMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parentUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Camper toEntity(CamperCreateRequest request);

    default CamperResponse toResponse(Camper camper, String status, boolean roomsAvailable, RoomHoldSummary roomHold) {
        return new CamperResponse(
            camper.getId(),
            camper.getFirstName(),
            camper.getLastName(),
            camper.getDateOfBirth(),
            camper.getGrade(),
            camper.getGender(),
            camper.getSpecialRequirements(),
            status,
            roomsAvailable,
            roomHold,
            camper.getCreatedOn());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parentUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    void applyPatch(CamperPatchRequest request, @MappingTarget Camper camper);
}
