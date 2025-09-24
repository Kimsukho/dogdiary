package com.project.service.impl;

import com.project.dao.UserDao;
import com.project.model.User;
import com.project.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userInfo = userDao.findUserByUsername(username);
		if (userInfo == null)
			throw new UsernameNotFoundException("No User Info");
		
		UserDetails userDetails = (UserDetails) new User();
		return userDetails;
	}
	
    private static Collection<? extends GrantedAuthority> getAuthorities(User userInfo) {
        List<String> roleNames = userInfo.getRoleList();
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		if (roleNames != null) {
			for (String role : roleNames) {
				GrantedAuthority authority = new SimpleGrantedAuthority(role);
				grantList.add(authority);
			}
		}
        return grantList;
    }    
    
	@Override
	public User findUserByUsername(String username) throws UsernameNotFoundException {
		User userInfo = userDao.getUserByName(username);
		return userInfo;
	}
	
	@Override
	public int register(HashMap<?, ?> map) {
		return userDao.register(map);
	}
	
	@Override
	public int update(HashMap<?, ?> map) {
		return userDao.update(map);
	}
	
	@Override
	public int deleteById(int id) {
		return userDao.deleteById(id);
	}
	
	@Override
	public List<HashMap> getUserList() {
		return userDao.getUserList();
	}

	@Override
	public int resetPassword(HashMap map) {
		return userDao.resetPassword(map);
	}
}
