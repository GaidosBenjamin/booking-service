package com.bgaidos.booking.conduct;

import com.bgaidos.booking.api.conduct.CodeOfConductResponse;
import com.bgaidos.booking.entity.CodeOfConduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CodeOfConductMapper {

    CodeOfConductResponse toResponse(CodeOfConduct entity);
}
