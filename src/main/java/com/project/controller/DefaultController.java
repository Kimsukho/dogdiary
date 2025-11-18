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
		
		// 로그인된 사용자는 대시보드로 리다이렉트
		logger.debug("인증된 사용자: {} - 대시보드로 리다이렉트", authentication.getName());
		return "redirect:/dashboard";
	}
	
	@GetMapping("/error")
	public String error() {

		return "error";
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle", "댕댕이 일지");
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
	    model.addAttribute("todayDiaries", 3);
	    model.addAttribute("dogCount", 2);
	    model.addAttribute("walkCount", "5번");
	    model.addAttribute("hospitalCount", "1건");
	    model.addAttribute("recentDiaries", List.of(
	        Map.of("title","2025-08-28 산책 2시간"),
	        Map.of("title","2025-08-27 목욕 기록")
	    ));
	    return "dashboard";
	}
	
	@GetMapping("/dogs")
	public String dogs(Model model) {
		model.addAttribute("title", "반려견 관리");
	    model.addAttribute("dogList", List.of(
	        Map.of("name","루루"), Map.of("name","몽이")
	    ));
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
		User loginUser = userService.findUserByUsername(authentication.getName());
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
//	public String settings(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
	public String settings(Model model) {
	    // principal 또는 세션에서 사용자 정보 가져오기
//	    User user = userService.findById(principal.getId());
//
//	    // 모델에 user 추가
//	    model.addAttribute("user", user);theme
		model.addAttribute("title", "설정");
		model.addAttribute("name", "홍길동");
		model.addAttribute("theme", "light");
		model.addAttribute("email", "test1234@naver.com");
	    return "settings"; // templates/settings.html
	}

}
