//package com.app.pofolit_be.user.dto;
//
//import com.app.pofolit_be.user.entity.User;
//
//public record ProfileDto(
//    String email,
//    String nickname,
//    String avatar,
//    String access
//) {
//
//  public static ProfileDto from(User user) {
//    return new ProfileDto(user.getEmail(), user.getNickname(), user.getAvatar(),
//        user.getAccess().getLv());
//  }
//}