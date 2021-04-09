package com.hopu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hopu.domain.Role;
import com.hopu.domain.User;

import java.util.List;


public interface IUserService extends IService<User> {
    void setRole(String id, List<Role> roles);
}
