package com.hopu.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hopu.domain.Menu;
import com.hopu.domain.RoleMenu;
import com.hopu.service.IMenuService;
import com.hopu.service.IRoleMenuService;
import com.hopu.service.impl.RoleMenuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import result.PageEntity;
import result.ResponseEntity;
import utils.IconFontUtils;
import utils.UUIDUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("menu")
public class MenuController {
    @Autowired
    private IMenuService menuService;

    @Autowired
    private IRoleMenuService roleMenuService;

    @GetMapping("/toMenuPage")
    public String toListPage(){
        return "admin/menu/menu_list";
    }

    @RequestMapping("/list")
    @ResponseBody
    public PageEntity list(){
        List<Menu> pList = menuService.list(new QueryWrapper<Menu>().eq("pid", "0"));
        ArrayList<Menu> menus = new ArrayList<>();
        findChildMenu(pList, menus);
        return new PageEntity(menus.size(),menus);
    }

    private void findChildMenu(List<Menu> pList, List<Menu> menus){
//        pList.forEach(menu -> {
//////            menus.add(menu); // 向返回集合中，添加父菜单
////
////            String pId = menu.getId();
////            List<Menu> childList = menuService.list(new QueryWrapper<Menu>().eq("pid", pId));
////            menu.setNodes(childList);
////
////            menus.addAll(childList); // 向返回集合中，添加子菜单
////
////            if(childList.size()>0){
////                // 递归调用
////                menus= findChildMenu(childList,menus);
////            }
////        });

        for (Menu menu : pList) {
            if(!menus.contains(menu)){
                menus.add(menu);
            }

            String pId = menu.getId();
            List<Menu> childList = menuService.list(new QueryWrapper<Menu>().eq("pid", pId));
            menu.setNodes(childList);

            if(childList.size()>0){
                // 递归调用
                findChildMenu(childList,menus);
            }
        }
        //return menus;
    }

    @RequestMapping("/toAddPage")
    public String toAddPage(Model model){
        //父级菜单
        List<Menu> list = menuService.list(new QueryWrapper<Menu>(new Menu()).eq("pid", '0'));
        findChildrens(list);

        //图标
        List<String> iconFont = IconFontUtils.getIconFont();

        model.addAttribute("iconFont", iconFont);
        model.addAttribute("list", list);
        return "admin/menu/menu_add";
    }
    private void findChildrens(List<Menu> list){
        for (Menu menu : list) {
            List<Menu> list2 = menuService.list(new QueryWrapper<Menu>(new Menu()).eq("pid", menu.getId()));
            if (list2!=null) {
                menu.setNodes(list2);
            }
        }
    }
    @ResponseBody
    @RequestMapping("/save")
    public ResponseEntity addMenu(Menu menu){
        menu.setId(UUIDUtils.getID());
        menu.setCreateTime(new Date());
        menuService.save(menu);
        return ResponseEntity.success();
    }

    @RequestMapping("/toUpdatePage")
    public String adminPage(String id, Model model){
        Menu menu = menuService.getById(id);
        model.addAttribute("menu", menu);

        List<Menu> list = menuService.list(new QueryWrapper<Menu>(new Menu()).eq("pid", '0').orderByAsc("seq"));
        findChildrens(list);

        //图标
        List<String> iconFont = IconFontUtils.getIconFont();

        model.addAttribute("iconFont", iconFont);
        model.addAttribute("list", list);
        return "admin/menu/menu_update";
    }

    /**
     * 修改
     */
    @ResponseBody
    @RequestMapping("update")
    public ResponseEntity updateMenu(Menu menu){
        menu.setUpdateTime(new Date());
        menuService.updateById(menu);
        return ResponseEntity.success();
    }

    @ResponseBody
    @RequestMapping("/delete")
    public ResponseEntity delete(@RequestBody ArrayList<Menu> menus){
        List<String> list = new ArrayList<String>();
        for (Menu menu : menus) {
            list.add(menu.getId());
        }
        menuService.removeByIds(list);
        return ResponseEntity.success();
    }

    @RequestMapping("/MenuList")
    @ResponseBody
    public PageEntity menuList(String roleId){
        // 查询当前角色已经关联了的权限
        List<RoleMenu> roleMenuList = roleMenuService.list(new QueryWrapper<RoleMenu>().eq("role_id", roleId));

        // 如果不涉及到子菜单关联
        List<Menu> list = menuService.list();

        //  此处循环的作用就是为了判断角色已有权限，然后添加一个LAY_CHECKED字段，前端layui表格才能自动勾选
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        list.forEach(menu -> {
            // 先需要把对象转换为JSON格式
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(menu));
            // 判断是否已经有了对应的权限
            List<String> menuIds = roleMenuList.stream().map(roleMenu -> roleMenu.getMenuId()).collect(Collectors.toList());
            if(menuIds.contains(menu.getId())){
                jsonObject.put("LAY_CHECKED",true);
            }
            jsonObjects.add(jsonObject);
        });

        return new PageEntity(jsonObjects.size(),jsonObjects);
    }
}
