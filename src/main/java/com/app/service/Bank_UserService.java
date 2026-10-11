package com.app.service;

import java.util.List;

import com.app.dto.LoginRequestDto;
import com.app.entity.Bank_User;

public interface Bank_UserService {

    public Bank_User register(Bank_User user);

    public Bank_User login(LoginRequestDto dto);

    List<Bank_User> getAllUser();
}