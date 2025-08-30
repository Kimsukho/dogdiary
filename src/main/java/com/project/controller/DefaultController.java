package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DefaultController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
	    model.addAttribute("title", "댕댕이 일지");
	    model.addAttribute("todayDiaries", 3);
	    model.addAttribute("dogCount", 2);
	    model.addAttribute("walkCount", 5);
	    model.addAttribute("hospitalCount", 1);
	    model.addAttribute("recentDiaries", List.of(
	        Map.of("title","2025-08-28 산책 2시간"),
	        Map.of("title","2025-08-27 목욕 기록")
	    ));
	    return "dashboard";
	}
	
	@GetMapping("/dogs")
	public String dogs(Model model) {
//	    model.addAttribute("dogList", List.of(
//	        Map.of("name","루루"), Map.of("name","몽이")
//	    ));
	    return "dogs";
	}
	
	@GetMapping("/diaries")
	public String diaries(Model model) {
//	    model.addAttribute("diaryList", List.of(
//	        Map.of("title","2025-08-28 산책"), Map.of("title","2025-08-27 목욕")
//	    ));
	    return "diaries";
	}

	@GetMapping("/profile")
	public String profile(Model model) {
	    model.addAttribute("user", Map.of("name","홍길동", "email","example@test.com"));
	    return "profile";
	}
	
	@GetMapping("/settings")
	public String settings(Model model) {
	    return "settings";
	}
}
