package com.project.service;

import com.project.dao.UserDao;
import com.project.model.User;

import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

public interface UserService extends UserDetailsService{
	public User findUserByUsername(String username);
	public int register(HashMap<?, ?> map);
	public int update(HashMap<?, ?> map);
	public int deleteById(int id);
	public List<HashMap> getUserList();
	public int resetPassword(HashMap map);
}