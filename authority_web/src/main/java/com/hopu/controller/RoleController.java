package com.hopu.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hopu.domain.Menu;
import com.hopu.domain.Role;
import com.hopu.domain.User;
import com.hopu.domain.UserRole;
import com.hopu.service.IRoleService;
import com.hopu.service.IUserRoleService;
import com.hopu.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import result.PageEntity;
import result.ResponseEntity;
import utils.UUIDUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IUserRoleService userRoleService;


    @RequestMapping("toListPage")
    public String toListPage() {
        return "admin/role/role_list";
    }


    @ResponseBody
    @RequestMapping("/list")
    public PageEntity userList(int page, int limit, Model model, Role role) {
        Page<Role> page2 = new Page<>(page, limit);
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>(new Role());
        if (StringUtils.isNotEmpty(role.getRole())&&role!=null) {
            queryWrapper.like("role",role.getRole());
        }
		IPage<Role> roleIPage = roleService.page(page2,queryWrapper);
		return new PageEntity(roleIPage);
    }

    @RequestMapping("/toAddPage")
    public String toAddPage(){
        return "admin/role/role_add";
    }

    @ResponseBody
    @RequestMapping("/save")
    public ResponseEntity addUser(Role role){
        Role role2 = roleService.getOne(new QueryWrapper<Role>().eq("role", role.getRole()));
        if (role2!=null){
            return ResponseEntity.error("角色名已经存在");
        }
        role.setId(UUIDUtils.getID());
        role.setCreateTime(new Date());
        roleService.save(role);
        return ResponseEntity.success();
    }

    @RequestMapping("/toUpdatePage")
    public String toUpdatePage(String id, Model model){
        Role role = roleService.getById(id);
        model.addAttribute("role",role);
        return "admin/role/role_update";
    }

    @RequestMapping("/update")
    @ResponseBody
    public ResponseEntity updateUser(Role role){
        role.setUpdateTime(new Date());
        roleService.updateById(role);
        return ResponseEntity.success();
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestBody List<Role> roles){
        try {
            // 如果是root用户，禁止删除
            for (Role role : roles) {
                if("root".equals(role.getRole())){
                    throw new Exception("root不能删除");
                }
//                if(user.getUserName().equals("root")){ // nullpointerException
//                    throw new Exception("不能删除超级管理员");
//                }
                roleService.removeById(role.getId());
            }
            return ResponseEntity.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
    }

    @RequestMapping("/toSetMenuPage")
    public String toSetMenuPage(String id, Model model){
        model.addAttribute("role_id",id);
        return "admin/role/role_setMenu";
    }
    /**
     * 设置权限
     */
    @ResponseBody
    @RequestMapping("/setMenu")
    public ResponseEntity setMenu(String id, @RequestBody ArrayList<Menu> menus){
        roleService.setMenu(id, menus);
        return ResponseEntity.success();
    }

    @ResponseBody
    @RequestMapping("/roleList")
    public PageEntity List(String userId, Role role){
        List<UserRole> userRoles = userRoleService.list(new QueryWrapper<UserRole>().eq("user_id", userId));

        QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>();
        if (role!=null){
            if (!StringUtils.isEmpty(role.getRole())) {queryWrapper.like("role", role.getRole());}
        }
        List<Role> roles = roleService.list(queryWrapper);

        List<JSONObject> list = new ArrayList<JSONObject>();
        // 同样需要对用户已经关联的角色进行勾选，根据layui需要填充一个LAY_CHECKED字段
        for (Role role2 : roles) {
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(role2));
            boolean rs = false;
            for (UserRole userRole : userRoles) {
                if (userRole.getRoleId().equals(role2.getId())) {
                    rs = true;
                }
            }
            jsonObject.put("LAY_CHECKED", rs);
            list.add(jsonObject);
        }
        return new PageEntity(list.size(), list);
    }

}

