package com.project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String roles; 
    
	// 사용자의 역할 목록 (예: "ROLE_USER", "ROLE_ADMIN")
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
	
	
	public List<String> getRoleList() {
		if (this.roles.length() > 0) {
			return Arrays.asList(this.roles.split(","));
		}
		return new ArrayList<>();
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	public String toStringUserInfo() {
		return getUsername() + getEmail();
	}

}