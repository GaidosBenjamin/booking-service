package com.bgaidos.booking.room;

import com.bgaidos.booking.api.room.RoomHoldResponse;
import com.bgaidos.booking.entity.RoomHold;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomHoldMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "camperId", source = "camper.id")
    RoomHoldResponse toResponse(RoomHold hold);
}
