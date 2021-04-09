package com.hopu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hopu.domain.Role;
import com.hopu.domain.User;
import com.hopu.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import result.ResponseEntity;
import utils.ShiroUtils;
import utils.UUIDUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;


    @RequiresPermissions("user:list")
    @RequestMapping("toListPage")
    public String toListPage() {
        return "admin/user/user_list";
    }


    @ResponseBody
    @RequiresPermissions("user:list")
    @RequestMapping("/list")
    public IPage<User> userList(int page, int limit, Model model, User user) {
        Page<User> page2 = new Page<User>(page, limit);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(new User());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getUserName())) {
                queryWrapper.like("user_name", user.getUserName());
            }
            if (!StringUtils.isEmpty(user.getTel())) {
                queryWrapper.like("tel", user.getTel());
            }
            if (!StringUtils.isEmpty(user.getEmail())) {
                queryWrapper.like("email", user.getEmail());
            }

        }
		IPage<User> userIPage = userService.page(page2,queryWrapper);
		return userIPage;
    }

    @RequiresPermissions("user:add")
    @RequestMapping("/toAddPage")
    public String toAddPage(){
        return "admin/user/user_add";
    }

    @ResponseBody
    @RequiresPermissions("user:add")
    @RequestMapping("/save")
    public ResponseEntity addUser(User user){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>(new User());
        queryWrapper.eq("user_name",user.getUserName());
        User user1=userService.getOne(queryWrapper);
        if (user1!=null){
            return ResponseEntity.error("用户名已经存在");
        }
        user.setId(UUIDUtils.getID());
        user.setSalt(UUIDUtils.getID());
        user.setCreateTime(new Date());
        ShiroUtils.encPass(user);
        userService.save(user);
        return ResponseEntity.success();
    }

    @RequiresPermissions("user:update")
    @RequestMapping("/toUpdatePage")
    public String toUpdatePage(String id, Model model){
        User user = userService.getById(id);
        model.addAttribute("user",user);
        return "admin/user/user_update";
    }

    @RequestMapping("/update")
    @RequiresPermissions("user:update")
    @ResponseBody
    public ResponseEntity updateUser(User user){
        user.setUpdateTime(new Date());
        ShiroUtils.encPass(user);
        userService.updateById(user);
        return ResponseEntity.success();
    }

    @RequestMapping("/delete")
    @RequiresPermissions("user:delete")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestBody List<User> users){
        try {
            // 如果是root用户，禁止删除
            for (User user : users) {
                if("root".equals(user.getUserName())){
                    throw new Exception("不能删除超级管理员");
                }
//                if(user.getUserName().equals("root")){ // nullpointerException
//                    throw new Exception("不能删除超级管理员");
//                }
                userService.removeById(user.getId());
            }
            return ResponseEntity.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
    }

    @RequestMapping("/toSetRole")
    @RequiresPermissions("user:setRole")
    public String toSetRole(String id, Model model){
        model.addAttribute("user_id", id);
        return "admin/user/user_setRole";
    }

    @ResponseBody
    @RequestMapping("setRole")
    @RequiresPermissions("user:setRole")
    public ResponseEntity setRole(String id, @RequestBody ArrayList<Role> roles){
        userService.setRole(id, roles);
        return ResponseEntity.success();
    }

}

