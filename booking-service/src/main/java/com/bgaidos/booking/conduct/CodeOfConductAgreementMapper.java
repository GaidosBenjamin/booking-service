package com.bgaidos.booking.conduct;

import com.bgaidos.booking.api.conduct.CodeOfConductAgreementResponse;
import com.bgaidos.booking.entity.CodeOfConductAgreement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CodeOfConductAgreementMapper {

    @Mapping(target = "codeOfConductId", source = "codeOfConduct.id")
    @Mapping(target = "userId", source = "user.id")
    CodeOfConductAgreementResponse toResponse(CodeOfConductAgreement entity);
}
