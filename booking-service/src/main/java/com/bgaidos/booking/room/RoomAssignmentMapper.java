package com.bgaidos.booking.room;

import com.bgaidos.booking.api.room.RoomAssignmentResponse;
import com.bgaidos.booking.entity.RoomAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomAssignmentMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "camperId", source = "camper.id")
    @Mapping(target = "leaderId", source = "leader.id")
    RoomAssignmentResponse toResponse(RoomAssignment assignment);
}
