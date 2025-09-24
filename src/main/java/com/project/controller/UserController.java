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
    private PasswordEncoder pwdEncode;

	@Autowired
	private UserService userService;

    @RequestMapping(value = "/public/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(HttpServletRequest request, @RequestBody HashMap map) throws Exception {
    	    	
    	try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(map.get("username"), map.get("password")));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                final UserDetails userDetails = userService.loadUserByUsername((String)map.get("username"));
  
                HttpSession session = request.getSession();
                session.setAttribute("userDetails", userDetails);
            User loginUser = userService.findUserByUsername((String)map.get("username"));                        
        	
            return ResponseEntity.ok(loginUser);

    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		
    		Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 실패");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // 401 코드로 내려주는 게 REST 표준
                    .body(errorResponse);

    	}

    }
    /*
    @RequestMapping(value = "/public/isTokenExpired", method = RequestMethod.GET)
	public ResponseEntity<?> isTokenExpired(HttpServletRequest request, @RequestParam(required = false) String token) throws Exception {
  	
    	boolean is_expired = false;
    	
    	try {
    		jwtTokenUtil.isTokenExpired(token);
    	}catch(Exception e) {
    		is_expired = true;
    		logger.debug(e.getMessage());
    		HttpSession session = request.getSession();
    		session.setAttribute("userDetails", null);
    	}    	
    	return ResponseEntity.ok(is_expired);
    }*/
    
    // 사용자 등록
    @Transactional(rollbackFor = Exception.class)
    @RequestMapping(value = "/admin/register", method = RequestMethod.POST)
    public HashMap register(@RequestBody HashMap<String, String> map ) throws Exception {
    	HashMap rtnVal = new HashMap();
    	//request params 은 되돌려 주지 않는 것으로 api 문서 정의
    	User userInfo = new User();
    	userInfo = userService.findUserByUsername(map.get("username"));
    	if(StringUtils.isEmpty(userInfo)) {
	    	try {
	    		String password = map.get("password");
		        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		        String encpass = encoder.encode(password);
		        map.put("password", encpass);
		        int val = userService.register(map);
				// 가장 최근에 생성된 사용자의 id 가져오기 위함 
		        List<HashMap> list = userService.getUserList();
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
    	try {
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
	public HashMap getUserList(){
    	HashMap rtnVal = new HashMap();
    	try {
        	rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", userService.getUserList());	        
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
	        rtnVal.put("resultData", userService.findUserByUsername(userName));	        
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
		return rtnVal;    	
    }
    
    /*
    // 내 정보 조회
    @GetMapping("/user/getInfo")
	public ResponseEntity<?> getUserInfo(@RequestParam String token) throws Exception {
    	String userName = jwtTokenUtil.getUserNameFromJwtToken(token);
    	UserInfo loginUser = userService.findUserByUsername(userName);
    	return ResponseEntity.ok(loginUser);
    }*/
    
    // 내 정보 조회
    @GetMapping("/user/getInfo")
	public HashMap getUserInfo(Principal principal) throws Exception {
    	HashMap rtnVal = new HashMap();
    	try {
        	rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", userService.findUserByUsername(principal.getName()));	        
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
		return rtnVal; 
    }
    
    // 비밀번호 변경
    @PostMapping("/user/resetPassword")
    public HashMap resetPassword(HttpSession session, @RequestBody HashMap map) throws Exception{
    	HashMap rtnVal = new HashMap();    	
    	try {
			String password = (String) map.get("password");
        	UserDetails sessionUser = (UserDetails) session.getAttribute("userDetails");
        	String username = sessionUser.getUsername();
    		final UserDetails userDetails = userService.loadUserByUsername(username);  
	        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	        boolean isMatch = encoder.matches(password, userDetails.getPassword());
	        if(!isMatch) {
	        	String encPassword = encoder.encode(password);
	        	map.put("username", userDetails.getUsername());
	        	map.put("password", encPassword);
	        	int val = userService.resetPassword(map);
		        if(val == 0) {
		        	rtnVal.put("returnCode", "FAILIRE");
			        rtnVal.put("errorMessage", val);
		        }else {
		        	rtnVal.put("returnCode", "SUCCESS");
			        rtnVal.put("resultData", val);	        	
		        }
	        } else {
	        	rtnVal.put("returnCode", "FAILIRE");
		        rtnVal.put("errorMessage", 0);
	        }
        } catch (Exception e) {
    		logger.error(e.getMessage());
    		rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
    	}
        return rtnVal;
    }    
}
