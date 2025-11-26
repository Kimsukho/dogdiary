package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.project.model.User;
import com.project.service.RestService;
import com.project.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DefaultController {
	

	@Autowired
	private Environment env;

	@Autowired
	private UserService userService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@GetMapping("/")
	public String root() {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 관리자 권한 확인
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		// 관리자는 사용자 관리 페이지로, 일반 사용자는 대시보드로 리다이렉트
		if (isAdmin) {
			logger.debug("관리자 사용자: {} - 사용자 관리 페이지로 리다이렉트", authentication.getName());
			return "redirect:/admin/users";
		} else {
			logger.debug("일반 사용자: {} - 대시보드로 리다이렉트", authentication.getName());
			return "redirect:/dashboard";
		}
	}
	
	@GetMapping("/error")
	public String error() {

		return "error";
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle", "펫케어 시스템");
		return "login";
	}
	
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
	    model.addAttribute("title", "대시보드");
	    return "dashboard";
	}
	
	@GetMapping("/dogs")
	public String dogs(Model model) {
		model.addAttribute("title", "반려견 관리");
	    return "dogs";
	}
	
	@GetMapping("/diaries")
	public String diaries(Model model) {
		model.addAttribute("title", "일지 관리");
	    return "diaries";
	}
	
	@GetMapping("/schedule")
	public String schedule(Model model) {
		model.addAttribute("title", "일정 관리");
	    return "schedule";
	}

	@GetMapping("/profile")
	public String profile(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 로그인한 사용자 정보 가져오기
		User loginUser = userService.getUserByName(authentication.getName());
		if (loginUser != null) {
			// 사용자 정보를 모델에 추가 (비밀번호는 제외)
			model.addAttribute("user", loginUser);
			logger.debug("프로필 페이지 로드 - 사용자: {}", loginUser.getUsername());
		} else {
			logger.warn("사용자 정보를 찾을 수 없음: {}", authentication.getName());
		}
		
		model.addAttribute("title", "프로필 설정");
	    return "profile";
	}
	
	@GetMapping("/settings")
	public String settings(Model model) {
	    return "settings";
	}
	
	@GetMapping("/admin/users")
	public String adminUsers(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 관리자 권한 확인
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!isAdmin) {
			logger.warn("관리자 권한이 없는 사용자가 관리자 페이지 접근 시도: {}", authentication.getName());
			return "redirect:/dashboard";
		}
		
		model.addAttribute("title", "사용자 관리");
		return "admin/users";
	}
	
	@GetMapping("/admin/diaries")
	public String adminDiaries(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 관리자 권한 확인
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!isAdmin) {
			logger.warn("관리자 권한이 없는 사용자가 관리자 페이지 접근 시도: {}", authentication.getName());
			return "redirect:/dashboard";
		}
		
		model.addAttribute("title", "일지 관리");
		return "admin/diaries";
	}
	
	@GetMapping("/admin/walks")
	public String adminWalks(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 관리자 권한 확인
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!isAdmin) {
			logger.warn("관리자 권한이 없는 사용자가 관리자 페이지 접근 시도: {}", authentication.getName());
			return "redirect:/dashboard";
		}
		
		model.addAttribute("title", "산책 관리");
		return "admin/walks";
	}
	
	@GetMapping("/admin/hospitals")
	public String adminHospitals(Model model) {
		// 로그인 상태 확인
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		// 인증되지 않았거나 anonymousUser인 경우 로그인 페이지로
		if (authentication == null || !authentication.isAuthenticated() 
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			logger.debug("인증되지 않은 사용자 - 로그인 페이지로 리다이렉트");
			return "redirect:/login";
		}
		
		// 관리자 권한 확인
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		
		if (!isAdmin) {
			logger.warn("관리자 권한이 없는 사용자가 관리자 페이지 접근 시도: {}", authentication.getName());
			return "redirect:/dashboard";
		}
		
		model.addAttribute("title", "병원 관리");
		return "admin/hospitals";
	}

}
