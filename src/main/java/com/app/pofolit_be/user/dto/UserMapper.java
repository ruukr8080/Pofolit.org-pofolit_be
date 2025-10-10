package com.app.pofolit_be.user.dto;

//import com.app.pofolit_be.user.dto.UserDto.UserProfileUpdateRequest;

import com.app.pofolit_be.user.dto.UserDto.UserCreateRequest;
import com.app.pofolit_be.user.dto.UserDto.UserResponse;
import com.app.pofolit_be.user.dto.UserDto.UserUpdateRequest;
import com.app.pofolit_be.user.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,// 매핑 전에 Null 체크
    unmappedTargetPolicy = ReportingPolicy.IGNORE) // 매핑안된 필드는 무시
public interface UserMapper {

  UserResponse toResponse(User user);

  //  @Mapping(target = "id", ignore = true) // ID는 DB가 생성
  @Mapping(source = "role", target = "role")
  User toEntity(UserCreateRequest request);// String(DTO) -> Role Enum(Entity) 자동 변환 시도

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(UserUpdateRequest request, @MappingTarget User user);

}