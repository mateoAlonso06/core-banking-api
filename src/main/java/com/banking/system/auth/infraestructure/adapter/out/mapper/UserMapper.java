package com.banking.system.auth.infraestructure.adapter.out.mapper;

import com.banking.system.auth.domain.model.User;
import com.banking.system.auth.infraestructure.adapter.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toDomain(UserJpaEntity userJpaEntity);

    UserJpaEntity toJpaEntity(User user);
}
