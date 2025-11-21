package com.project.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String role; // 단일 role 컬럼 (예: "USER", "ADMIN")
    private Timestamp regdate;
    private Timestamp last_updated;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getRegdate() {
        return regdate;
    }

    public void setRegdate(Timestamp regdate) {
        this.regdate = regdate;
    }

    public Timestamp getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Timestamp last_updated) {
        this.last_updated = last_updated;
    }

    // 헬퍼 메서드: ROLE_USER, ROLE_ADMIN 같은 문자열 리스트 리턴
    public List<String> getRoleNames() {
        List<String> names = new ArrayList<>();
        
        // role 컬럼이 있으면 사용
        if (role != null && !role.isEmpty()) {
            // ROLE_ 접두사가 없으면 추가
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            names.add(roleName);
        }
        
        return names;
    }

    public String toStringUserInfo() {
        return username + " (" + email + ")";
    }
}
