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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userInfo = userDao.getUserByName(username);
		if (userInfo == null) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
		}
		
		// 비밀번호가 null이거나 비어있으면 예외 발생
		if (userInfo.getPassword() == null || userInfo.getPassword().isEmpty()) {
			throw new UsernameNotFoundException("사용자 비밀번호가 설정되지 않았습니다: " + username);
		}
		
		List<GrantedAuthority> authorities = getAuthorities(userInfo);
		
		// 최소 하나의 권한이 필요하므로, 권한이 없으면 기본 USER 권한 부여
		if (authorities.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}
		
		return org.springframework.security.core.userdetails.User.builder()
				.username(userInfo.getUsername())
				.password(userInfo.getPassword())
				.authorities(authorities)
				.build();
	}
	
    private List<GrantedAuthority> getAuthorities(User userInfo) {
        List<String> roleNames = userInfo.getRoleNames();
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		if (roleNames != null && !roleNames.isEmpty()) {
			for (String role : roleNames) {
				// ROLE_ 접두사가 없으면 추가
				String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
				GrantedAuthority authority = new SimpleGrantedAuthority(roleName);
				grantList.add(authority);
			}
		}
        return grantList;
    }    
    
	@Override
	public User getUserByName(String username) throws UsernameNotFoundException {
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
	public List<HashMap> getUserList(HashMap map) {
		return userDao.getUserList(map);
	}

	@Override
	public int getUserListCount() {
		return userDao.getUserListCount();
	}

	@Override
	public HashMap getUserInfo(String username) {
		return userDao.getUserInfo(username);
	}

	@Override
	public int resetPassword(HashMap map) {
		return userDao.resetPassword(map);
	}
}
