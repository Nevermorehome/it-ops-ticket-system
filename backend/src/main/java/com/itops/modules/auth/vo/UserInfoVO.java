package com.itops.modules.auth.vo;

import com.itops.modules.system.entity.SysMenu;
import com.itops.modules.system.entity.SysUser;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserInfoVO {

    private SysUser user;
    private List<String> roles;
    private Set<String> permissions;
    private List<SysMenu> menus;
}
