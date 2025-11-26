package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.dao.UserDao;
import com.project.model.User;
import com.project.service.RestService;
import com.project.service.UserService;
import com.project.service.impl.UserServiceImpl;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.valves.rewrite.InternalRewriteMap.Escape;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestMapping(value = "/api")
@RestController
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

    @RequestMapping(value = "/public/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(HttpServletRequest request, @RequestBody HashMap map) throws Exception {
    	String username = (String) map.get("username");
    	String password = (String) map.get("password");
    	
    	logger.debug("로그인 시도: username={}", username);
    	
    	if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
    		Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 실패");
            errorResponse.put("message", "아이디와 비밀번호를 입력해주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    	}
    	
    	try {
            // 사용자 존재 여부 먼저 확인
            User user = userService.getUserByName(username);
            if (user == null) {
            	logger.warn("사용자를 찾을 수 없음: {}", username);
            	Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "로그인 실패");
                errorResponse.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            logger.debug("사용자 찾음: username={}, password 존재={}, password 길이={}", 
            		username, user.getPassword() != null, 
            		user.getPassword() != null ? user.getPassword().length() : 0);
            
            // 비밀번호가 BCrypt 형식인지 확인 (BCrypt는 항상 $2a$, $2b$, $2y$로 시작)
            if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            	logger.warn("경고: 사용자 비밀번호가 BCrypt 형식이 아닙니다. username={}", username);
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            final UserDetails userDetails = userService.loadUserByUsername(username);
  
            HttpSession session = request.getSession();
            session.setAttribute("userDetails", userDetails);
            User loginUser = userService.getUserByName(username);
            
            logger.info("로그인 성공: username={}", username);
            return ResponseEntity.ok(loginUser);

    	} catch (org.springframework.security.core.AuthenticationException e) {
    		logger.error("인증 실패: username={}, error={}", username, e.getMessage(), e);
    		
    		Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 실패");
            errorResponse.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
    	} catch (Exception e) {
    		logger.error("로그인 처리 중 오류 발생: username={}, error={}", username, e.getMessage(), e);
    		
    		Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 실패");
            errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
    	}
    }
    
    // 개발용: 비밀번호 BCrypt 인코딩 유틸리티 (개발 완료 후 제거 권장)
    @RequestMapping(value = "/public/encodePassword", method = RequestMethod.POST)
    public ResponseEntity<?> encodePassword(@RequestBody HashMap map) {
    	try {
    		String plainPassword = (String) map.get("password");
    		if (plainPassword == null || plainPassword.isEmpty()) {
    			return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력해주세요."));
    		}
    		
    		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    		String encodedPassword = encoder.encode(plainPassword);
    		
    		// BCrypt 해시 형식 검증
    		boolean isValidFormat = encodedPassword.startsWith("$2a$") || 
    		                        encodedPassword.startsWith("$2b$") || 
    		                        encodedPassword.startsWith("$2y$");
    		
    		Map<String, Object> result = new HashMap<>();
    		result.put("plainPassword", plainPassword);
    		result.put("encodedPassword", encodedPassword);
    		result.put("isValidBCrypt", isValidFormat);
    		result.put("hashLength", encodedPassword.length());
    		
    		// SQL 업데이트 문 예시도 제공
    		result.put("sqlExample", String.format(
    			"UPDATE users SET password = '%s' WHERE username = '사용자명';", 
    			encodedPassword));
    		
    		logger.info("BCrypt 인코딩 완료: password={}, hash={}", plainPassword, encodedPassword);
    		
    		return ResponseEntity.ok(result);
    	} catch (Exception e) {
    		logger.error("비밀번호 인코딩 실패: " + e.getMessage(), e);
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    				.body(Map.of("error", "비밀번호 인코딩 실패: " + e.getMessage()));
    	}
    }
    
    // 개발용: 사용자 비밀번호 직접 업데이트 (개발 완료 후 제거 권장)
    @RequestMapping(value = "/public/updateUserPassword", method = RequestMethod.POST)
    public ResponseEntity<?> updateUserPassword(@RequestBody HashMap map) {
    	logger.info("비밀번호 업데이트 요청 수신: map={}", map);
    	try {
    		String username = (String) map.get("username");
    		String plainPassword = (String) map.get("password");
    		logger.debug("요청 파라미터: username={}, password 존재={}", username, plainPassword != null);
    		
    		if (username == null || username.isEmpty()) {
    			return ResponseEntity.badRequest().body(Map.of("error", "사용자명을 입력해주세요."));
    		}
    		if (plainPassword == null || plainPassword.isEmpty()) {
    			return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력해주세요."));
    		}
    		
    		// 사용자 존재 확인
    		User user = userService.getUserByName(username);
    		if (user == null) {
    			return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다: " + username));
    		}
    		
    		// BCrypt 인코딩
    		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    		String encodedPassword = encoder.encode(plainPassword);
    		
    		// 비밀번호 업데이트
    		HashMap updateMap = new HashMap();
    		updateMap.put("id", user.getId());
    		updateMap.put("password", encodedPassword);
    		
    		int result = userService.update(updateMap);
    		
    		if (result > 0) {
    			Map<String, Object> response = new HashMap<>();
    			response.put("returnCode", "SUCCESS");
    			response.put("message", "비밀번호가 성공적으로 업데이트되었습니다.");
    			response.put("username", username);
    			logger.info("비밀번호 업데이트 성공: username={}", username);
    			return ResponseEntity.ok(response);
    		} else {
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    					.body(Map.of("error", "비밀번호 업데이트 실패"));
    		}
    	} catch (Exception e) {
    		logger.error("비밀번호 업데이트 실패: " + e.getMessage(), e);
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    				.body(Map.of("error", "비밀번호 업데이트 실패: " + e.getMessage()));
    	}
    }
    
    // 사용자 등록
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/admin/register", method = RequestMethod.POST)
    public HashMap register(@RequestBody HashMap<String, String> map ) throws Exception {
    	HashMap rtnVal = new HashMap();
    	//request params 은 되돌려 주지 않는 것으로 api 문서 정의
    	User userInfo = new User();
    	userInfo = userService.getUserByName(map.get("username"));
    	if(StringUtils.isEmpty(userInfo)) {
    	try {
    		String password = map.get("password");
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        String encpass = encoder.encode(password);
	        map.put("password", encpass);
	        
	        // role이 없으면 기본값 "USER" 설정
	        if (!map.containsKey("role") || map.get("role") == null || map.get("role").toString().isEmpty()) {
	        	map.put("role", "USER");
	        }
	        
	        int val = userService.register(map);
			// 가장 최근에 생성된 사용자의 id 가져오기 위함 
		        HashMap queryMap = new HashMap();
		        List<HashMap> list = userService.getUserList(queryMap);
			HashMap CreatedUser = null;
			if( !list.isEmpty() )
				CreatedUser = list.get(list.size() - 1);
		        if(val == 0) {
		        	rtnVal.put("returnCode", "FAILIRE");
			        rtnVal.put("errorMessage", val);
		        }else {
		        	rtnVal.put("returnCode", "SUCCESS");
			        rtnVal.put("resultData", CreatedUser);	        	
		        }	
	    	} catch (Exception e) {
	    		logger.error(e.getMessage());
	    		rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", e.getMessage());
	    	}
    	} else {
    		rtnVal.put("returnCode", "FAILURE");
    		rtnVal.put("errorMessage", map.get("username") + " 등록되어 있는 계정입니다.");
    	}
        return rtnVal;
    }    
    
    // 사용자 정보 수정
    @PostMapping("/admin/update")
    public HashMap update(@RequestBody HashMap map) throws Exception{
    	HashMap rtnVal = new HashMap();
    	
    	// 로그인한 사용자 정보 가져오기
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
    		rtnVal.put("returnCode", "FAILURE");
    		rtnVal.put("errorMessage", "로그인이 필요합니다.");
    		return rtnVal;
    	}
    	
    	// 관리자 권한 확인
    	boolean isAdmin = authentication.getAuthorities().stream()
    			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    	
    	// 관리자가 아니면 자신의 정보만 수정 가능
    	if (!isAdmin) {
    		User loginUser = userService.getUserByName(authentication.getName());
    		if (loginUser == null) {
    			rtnVal.put("returnCode", "FAILURE");
    			rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
    			return rtnVal;
    		}
    		// 자신의 ID로만 수정 가능하도록 강제
    		map.put("id", loginUser.getId());
    	}
    	
    	try {
    		// 비밀번호가 제공된 경우 암호화
    		if (map.containsKey("password") && map.get("password") != null && !map.get("password").toString().isEmpty()) {
    			String plainPassword = map.get("password").toString();
    			// BCrypt 해시 형식이 아니면 암호화 (관리자가 평문 비밀번호를 입력한 경우)
    			if (!plainPassword.startsWith("$2")) {
    				String encodedPassword = passwordEncoder.encode(plainPassword);
    				map.put("password", encodedPassword);
    				logger.debug("비밀번호 암호화 완료");
    			}
    		}
    		
        	int val = userService.update(map);
	        if(val == 0) {
	        	rtnVal.put("returnCode", "FAILIRE");
		        rtnVal.put("errorMessage", val);
	        }else {
	        	rtnVal.put("returnCode", "SUCCESS");
		        rtnVal.put("resultData", val);	        	
	        }
        } catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
        return rtnVal;
    }
    
    // 사용자 삭제
    @DeleteMapping("/admin/delete/{userId}")
	public HashMap deleteById(@PathVariable(value = "userId") String userId) throws Exception {
    	HashMap rtnVal = new HashMap();
    	
    	// 로그인한 사용자 정보 가져오기
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
    		rtnVal.put("returnCode", "FAILURE");
    		rtnVal.put("errorMessage", "로그인이 필요합니다.");
    		return rtnVal;
    	}
    	
    	// 관리자 권한 확인
    	boolean isAdmin = authentication.getAuthorities().stream()
    			.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    	
    	if (!isAdmin) {
    		rtnVal.put("returnCode", "FAILURE");
    		rtnVal.put("errorMessage", "관리자 권한이 필요합니다.");
    		return rtnVal;
    	}
    	
    	try {
        	int val = userService.deleteById(Integer.parseInt(userId));
	        if(val == 0) {
	        	rtnVal.put("returnCode", "FAILIRE");
		        rtnVal.put("errorMessage", val);
	        }else {
	        	rtnVal.put("returnCode", "SUCCESS");
		        rtnVal.put("resultData", val);	        	
	        }	        
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
        return rtnVal;
    }
    
    // 모든 사용자 조회
    @GetMapping("/admin/getUserList")
	public HashMap getUserList(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size){
    	HashMap rtnVal = new HashMap();
    	try {
    		// 페이지네이션 파라미터 설정 (기본값: page=1, size=10)
    		int currentPage = (page != null && page > 0) ? page : 1;
    		int pageSize = (size != null && size > 0) ? size : 10;
    		int offset = (currentPage - 1) * pageSize;
    		
    		HashMap map = new HashMap();
    		map.put("limit", pageSize);
    		map.put("offset", offset);
    		
    		List<HashMap> userList = userService.getUserList(map);
    		int totalCount = userService.getUserListCount();
    		int totalPages = (int) Math.ceil((double) totalCount / pageSize);
    		
    		HashMap result = new HashMap();
    		result.put("list", userList);
    		result.put("totalCount", totalCount);
    		result.put("totalPages", totalPages);
    		result.put("currentPage", currentPage);
    		result.put("pageSize", pageSize);
    		
        	rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", result);	        
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
		return rtnVal;
    }
   
    // 사용자 정보 조회
    @GetMapping("/admin/getUserByName")
	public HashMap getUserByName(@RequestParam String userName) throws Exception {
    	HashMap rtnVal = new HashMap();
    	try {
        	rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", userService.getUserByName(userName));	        
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
		return rtnVal;    	
    }
    
    // 내 정보 조회
    @GetMapping("/user/getInfo")
	public HashMap getUserInfo(Principal principal) throws Exception {
    	HashMap rtnVal = new HashMap();
    	try {
    		// getUserInfo는 HashMap을 반환하므로 regdate 등 모든 필드 포함
    		HashMap userInfo = userService.getUserInfo(principal.getName());
    		if (userInfo != null) {
    			// password는 보안상 제외
    			userInfo.remove("password");
    			rtnVal.put("returnCode", "SUCCESS");
    			rtnVal.put("resultData", userInfo);
    		} else {
    			rtnVal.put("returnCode", "FAILURE");
    			rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
    		}
    	} catch (Exception e) {
    		logger.error("사용자 정보 조회 실패: " + e.getMessage(), e);
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
		return rtnVal; 
    }
    
    // 비밀번호 변경
    @PostMapping("/user/resetPassword")
    public HashMap resetPassword(@RequestBody HashMap map) throws Exception{
    	HashMap rtnVal = new HashMap();    	
    	try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			String username = authentication.getName();
			String currentPassword = (String) map.get("currentPassword");
			String newPassword = (String) map.get("password");
			
			logger.debug("비밀번호 변경 요청 - username: {}, currentPassword 제공: {}, newPassword 제공: {}", 
					username, currentPassword != null && !currentPassword.isEmpty(), newPassword != null && !newPassword.isEmpty());
			
			// 입력값 검증
			if (currentPassword == null || currentPassword.isEmpty()) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "현재 비밀번호를 입력해주세요.");
				return rtnVal;
			}
			
			if (newPassword == null || newPassword.isEmpty()) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "새 비밀번호를 입력해주세요.");
				return rtnVal;
			}
			
			if (newPassword.length() < 4) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "비밀번호는 최소 4자 이상이어야 합니다.");
				return rtnVal;
			}
			
			// 데이터베이스에서 사용자 정보 조회 (암호화된 비밀번호 포함)
			User user = userService.getUserByName(username);
			if (user == null) {
				logger.error("사용자를 찾을 수 없음: {}", username);
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			String encodedPasswordFromDb = user.getPassword();
			if (encodedPasswordFromDb == null || encodedPasswordFromDb.isEmpty()) {
				logger.error("데이터베이스에 저장된 비밀번호가 없음: {}", username);
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "비밀번호 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			logger.debug("데이터베이스 비밀번호 해시: {}", encodedPasswordFromDb.substring(0, Math.min(20, encodedPasswordFromDb.length())) + "...");
			
			// 현재 비밀번호 확인 (BCrypt로 비교)
			boolean isMatch = passwordEncoder.matches(currentPassword, encodedPasswordFromDb);
			logger.debug("현재 비밀번호 일치 여부: {}", isMatch);
			
			if (!isMatch) {
				logger.warn("현재 비밀번호 불일치 - username: {}", username);
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
				return rtnVal;
			}
			
			// 새 비밀번호가 현재 비밀번호와 같은지 확인
			boolean isSamePassword = passwordEncoder.matches(newPassword, encodedPasswordFromDb);
			if (isSamePassword) {
				logger.warn("새 비밀번호가 현재 비밀번호와 동일 - username: {}", username);
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
				return rtnVal;
			}
			
			// 새 비밀번호 인코딩
			String encPassword = passwordEncoder.encode(newPassword);
			logger.debug("새 비밀번호 인코딩 완료");
			
			// 데이터베이스 업데이트
			HashMap updateMap = new HashMap();
			updateMap.put("username", username);
			updateMap.put("password", encPassword);
			
			int val = userService.resetPassword(updateMap);
			if (val == 0) {
				logger.error("비밀번호 업데이트 실패 - username: {}, 업데이트된 행 수: 0", username);
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "비밀번호 변경에 실패했습니다.");
			} else {
				rtnVal.put("returnCode", "SUCCESS");
				rtnVal.put("resultData", val);
				logger.info("비밀번호 변경 성공 - username: {}, 업데이트된 행 수: {}", username, val);
			}
        } catch (Exception e) {
    		logger.error("비밀번호 변경 실패 - username: {}, error: {}", 
    				SecurityContextHolder.getContext().getAuthentication() != null ? 
    				SecurityContextHolder.getContext().getAuthentication().getName() : "unknown", 
    				e.getMessage(), e);
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage() != null ? e.getMessage() : "비밀번호 변경 중 오류가 발생했습니다.");
    	}
        return rtnVal;
    }    
}
