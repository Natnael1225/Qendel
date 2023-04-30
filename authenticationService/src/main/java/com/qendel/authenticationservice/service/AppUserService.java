package com.qendel.authenticationservice.service;

import com.qendel.authenticationservice.dto.AppUserDto;
import com.qendel.authenticationservice.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserService {
    List<AppUserDto> findAllUsers();
    Optional<AppUserDto> findUserById(Long id);
    Optional<AppUserDto> findUserByName(String name);
    List<AppUserDto> findAllUsersByRole(String role);
    Optional<AppUserDto> findUserByEmail(String email);
    AppUserDto viewProfile(Long id);
    AppUserDto updateProfile(Long id, AppUser appUser);
}