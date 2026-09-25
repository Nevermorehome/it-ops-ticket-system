package com.itops.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 登录用户身份(由 SecurityUserService 装配)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private Long deptId;
    private String realName;
    private Boolean enabled;

    /** 角色 key 集合, 如 admin/supervisor/engineer/reporter */
    private Set<String> roles;

    /** 权限标识集合, 如 ticket:list */
    private Set<String> permissions;

    /** 角色对应的数据范围(1全部 2自定义 3本部门 4本部门及以下 5仅本人) 及自定义部门 */
    private List<RoleScope> roleScopes = new ArrayList<>();

    public boolean isAdmin() {
        return userId != null && userId == 1L || (roles != null && roles.contains("admin"));
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        if (permissions != null) {
            permissions.stream().filter(p -> p != null && !p.isBlank())
                    .forEach(p -> list.add(new SimpleGrantedAuthority(p)));
        }
        if (roles != null) {
            roles.forEach(r -> list.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        return list;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /** 角色数据范围定义 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleScope {
        /** 1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
        private String dataScope;
        /** dataScope=2 时自定义的部门ID */
        private List<Long> deptIds;
    }
}
