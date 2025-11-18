package com.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

	private static final String[] PUBLIC_URI = {
			"/api/public/**", "/",
			"/login", "/error" };

	private static final String[] PUBLIC_GET_URI = { "/exception/**", "/helloworld/**", "/actuator/health" };
	
	// 인증이 필요한 API 엔드포인트 (POST, PUT, DELETE)
	private static final String[] AUTHENTICATED_API_URI = { 
			"/api/saveDog", "/api/updateDogById", "/api/deleteDogById",
			"/api/saveDogDiaryByDogId", "/api/updateDogDiaryByDogId", "/api/deleteDogDiaryById",
			"/api/createWalk", "/api/updateWalk", "/api/deleteWalk",
			"/api/createHospital", "/api/updateHospital", "/api/deleteHospital",
			"/api/admin/**" };
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

	@Override
	public void configure(WebSecurity web) throws Exception {
		// 정적 리소스는 Spring Security 필터 체인에서 제외
		web.ignoring().antMatchers(
			"/css/**", "/js/**", "/images/**", "/fonts/**", 
			"/icons/**", "/static/**", "/manifest.json",
			"/favicon.ico", "/favicon.png",
			"/custom/**", "/libs/**", "/style/**", "/lib/**",
			"/bootstrap/**", "/asset/**", "/aseet/**",
			"/test_password_update.html", // 비밀번호 업데이트 테스트 페이지
			"/error" // 에러 페이지는 인증 없이 접근 가능
		);
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
		httpSecurity.sessionManagement().sessionFixation().migrateSession();
		httpSecurity
				.cors().and() // CORS 활성화
				.csrf().disable() // rest api이므로 csrf 보안이 필요없으므로 disable처리.
				.authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크(리소스 별 허용 범위 설정)
				.antMatchers(PUBLIC_URI).permitAll() // 가입 및 인증 주소는 누구나 접근가능
				.antMatchers(HttpMethod.GET, PUBLIC_GET_URI).permitAll() // 등록된 GET요청 리소스는 누구나 접근가능
				.antMatchers(AUTHENTICATED_API_URI).hasAnyRole("USER", "ADMIN") // 인증이 필요한 API
				.antMatchers("/api/admin/**").hasRole("ADMIN") // 관리자만 접근 가능한 API
				.antMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN") // GET API는 인증 필요
        		.anyRequest().authenticated() // 나머지 요청은 인증 필요
        		.and()
        		.exceptionHandling()
        			.authenticationEntryPoint(apiAuthenticationEntryPoint()) // REST API 인증 실패 시 JSON 응답
        		.and().formLogin()
        			.loginPage("/login")
        			// loginProcessingUrl은 REST API를 사용하므로 설정하지 않음
        			// 실제 로그인은 /api/public/authenticate 엔드포인트를 통해 처리
        			.defaultSuccessUrl("/dashboard", true)
        			.failureUrl("/login?error=true")
        			.permitAll()
				.and()      
		        .logout()
		        	.logoutUrl("/logout")
		        	.logoutSuccessHandler(logoutSuccessHandler())
		        	.invalidateHttpSession(true)
		        	.clearAuthentication(true)
		        	.deleteCookies("JSESSIONID")
		        	.permitAll();
		httpSecurity
				.headers().frameOptions().disable();
	}
	
	@Bean
	public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
		return new AuthenticationEntryPoint() {
			@Override
			public void commence(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException authException) throws IOException, ServletException {
				// REST API 요청인 경우 JSON 응답 반환
				if (request.getRequestURI().startsWith("/api/")) {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					
					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("returnCode", "FAILURE");
					errorResponse.put("errorMessage", "인증이 필요합니다. 로그인해주세요.");
					
					ObjectMapper objectMapper = new ObjectMapper();
					response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
				} else {
					// 일반 웹 페이지 요청인 경우 로그인 페이지로 리다이렉트
					response.sendRedirect("/login");
				}
			}
		};
	}
	
	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return new LogoutSuccessHandler() {
			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
				// SecurityContext 명시적으로 클리어
				SecurityContextHolder.clearContext();
				
				// 세션 무효화
				request.getSession().invalidate();
				
				// 로그인 페이지로 리다이렉트
				response.sendRedirect("/login");
			}
		};
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 모든 origin 허용 (개발용)
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
