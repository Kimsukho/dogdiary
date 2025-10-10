package com.project.dao;

import com.project.model.User;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {
	public User findUserByUsername(String username);
	public int register(HashMap<?, ?> map);
	public int update(HashMap<?, ?> map);
	public int deleteById(int id);
	public User getUserByName(String username);
	public List<HashMap> getUserList();
	public HashMap getUserInfo(String username);
	public int resetPassword(HashMap map);
}