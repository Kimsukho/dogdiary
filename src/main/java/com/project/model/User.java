package com.project.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;
    private String username;
    private String password;
    private String email;

    private List<Role> roles = new ArrayList<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<Role> getRoles() {
        return roles;
    }
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    // 헬퍼 메서드: ROLE_USER, ROLE_ADMIN 같은 문자열 리스트 리턴
    public List<String> getRoleNames() {
        List<String> names = new ArrayList<>();
        for (Role role : roles) {
            names.add(role.getRoleName());
        }
        return names;
    }

    public String toStringUserInfo() {
        return username + " (" + email + ")";
    }
}
