package com.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

	private static final String[] PUBLIC_URI = { "/realtime/**", "/api/user/**",
			"/api/public/**", "/", 
			"/favicon.ico", "/favicon.png",
			"/login", "/dashboard", "/profile", "/dogs", "/diaries", "/schedule", "/settings",
			"/css/**", "/js/**", "/api/**",
			"/manifest.json", "/images/**", "/fonts/**", "/icons/**", "/static/**",
			"/custom/**","/libs/**", "/style/**", "/lib/**" };

	private static final String[] PUBLIC_GET_URI = { "/exception/**", "/helloworld/**", "/actuator/health" };
    
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
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
		httpSecurity.sessionManagement().sessionFixation().migrateSession();
		httpSecurity
				// .httpBasic().disable() // rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트
				// 된다.
				.csrf().disable() // rest api이므로 csrf 보안이 필요없으므로 disable처리.
				.authorizeRequests() // 다음 리퀘스트에 대한 사용권한 체크(리소스 별 허용 범위 설정)
				// .antMatchers("/**").permitAll();
				.antMatchers(PUBLIC_URI).permitAll() // 가입 및 인증 주소는 누구나 접근가능
				.antMatchers(HttpMethod.GET, PUBLIC_GET_URI).permitAll() // 등록된 GET요청 리소스는 누구나 접근가능
        		.anyRequest().hasAnyRole("USER", "ADMIN")				
        		.and().formLogin().loginPage("/login") 			
				.and()      
		        .logout().logoutUrl("/logout")  
		        .logoutSuccessUrl("/login");
				httpSecurity
				// .httpBasic().disable() // rest api 이므로 기본설정 사용안함. 기본설정은 비인증시 로그인폼 화면으로 리다이렉트
				// 된다.
				.headers().frameOptions().disable();
	}
}
