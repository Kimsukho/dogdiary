package com.project.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.service.RestService;
import com.project.service.UserService;
import com.project.service.NotificationService;
import com.project.model.User;

@RequestMapping(value = "/api")
@RestController
public class DiaryController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RestService restService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private NotificationService notificationService;
	
	@GetMapping("/getDogsByUserId")
	public HashMap getDogsByUserId(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 user_id로만 조회
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId());
			}
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
			
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getDogsByUserId(map));  
        } catch (Exception e) {
        	logger.error("반려견 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;	
	}
	
	@PostMapping("/saveDog")
	public HashMap saveDog(
			@RequestParam("name") String name,
			@RequestParam("breed") String breed,
			@RequestParam(value = "age", required = false) Integer age,
			@RequestParam(value = "age_unit", required = false) String ageUnit,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "weight", required = false) Double weight,
			@RequestParam(value = "profile_image", required = false) MultipartFile profileImage) {
		HashMap rtnVal = new HashMap();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			HashMap map = new HashMap();
			map.put("user_id", loginUser.getId());
			map.put("name", name);
			map.put("breed", breed);
			if (age != null) map.put("age", age);
			if (ageUnit != null && !ageUnit.isEmpty()) map.put("age_unit", ageUnit);
			if (gender != null && !gender.isEmpty()) map.put("gender", gender);
			if (weight != null) map.put("weight", weight);
			
			// 프로필 이미지 저장
			if (profileImage != null && !profileImage.isEmpty()) {
				try {
					// 파일명에서 특수문자 제거 및 정리
					String originalFileName = profileImage.getOriginalFilename();
					if (originalFileName == null || originalFileName.isEmpty()) {
						throw new IllegalArgumentException("파일명이 없습니다.");
					}
					
					// 파일 확장자 추출
					String extension = "";
					int lastDotIndex = originalFileName.lastIndexOf('.');
					if (lastDotIndex > 0) {
						extension = originalFileName.substring(lastDotIndex);
					}
					
					// 안전한 파일명 생성 (특수문자 제거)
					String safeFileName = System.currentTimeMillis() + extension;
					
					// 상대 경로 사용 (프로젝트 루트 기준)
					String uploadDir = "src/main/resources/static/images/";
					File dir = new File(uploadDir);
					if (!dir.exists()) {
						boolean created = dir.mkdirs();
						if (!created) {
							logger.error("디렉토리 생성 실패: " + uploadDir);
							throw new IOException("이미지 저장 디렉토리를 생성할 수 없습니다.");
						}
					}
					
					Path filePath = Paths.get(uploadDir + safeFileName);
					Files.write(filePath, profileImage.getBytes());
					
					map.put("profile_image", "/images/" + safeFileName);
					logger.info("이미지 저장 성공: " + safeFileName);
				} catch (IOException e) {
					logger.error("이미지 저장 실패: " + e.getMessage(), e);
					rtnVal.put("returnCode", "FAILURE");
					rtnVal.put("errorMessage", "이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
					return rtnVal;
				}
			}
			
			int val = restService.saveDog(map);
			rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
			rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
		} catch (Exception e) {
			logger.error("반려견 저장 실패: " + e.getMessage(), e);
			rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.");
		}
		return rtnVal;
	}
	
	@PostMapping("/updateDogById")
	public HashMap updateDogById(
			@RequestParam("id") Integer id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "breed", required = false) String breed,
			@RequestParam(value = "age", required = false) Integer age,
			@RequestParam(value = "age_unit", required = false) String ageUnit,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "weight", required = false) Double weight,
			@RequestParam(value = "profile_image", required = false) MultipartFile profileImage) {
		HashMap rtnVal = new HashMap();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 수정 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			HashMap map = new HashMap();
			map.put("id", id);
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
			if (name != null) map.put("name", name);
			if (breed != null) map.put("breed", breed);
			if (age != null) map.put("age", age);
			if (ageUnit != null && !ageUnit.isEmpty()) map.put("age_unit", ageUnit);
			if (gender != null) map.put("gender", gender);
			if (weight != null) map.put("weight", weight);
			
			// 프로필 이미지 저장
			if (profileImage != null && !profileImage.isEmpty()) {
				try {
					// 파일명에서 특수문자 제거 및 정리
					String originalFileName = profileImage.getOriginalFilename();
					if (originalFileName == null || originalFileName.isEmpty()) {
						throw new IllegalArgumentException("파일명이 없습니다.");
					}
					
					// 파일 확장자 추출
					String extension = "";
					int lastDotIndex = originalFileName.lastIndexOf('.');
					if (lastDotIndex > 0) {
						extension = originalFileName.substring(lastDotIndex);
					}
					
					// 안전한 파일명 생성 (특수문자 제거)
					String safeFileName = System.currentTimeMillis() + extension;
					
					// 상대 경로 사용 (프로젝트 루트 기준)
					String uploadDir = "src/main/resources/static/images/";
					File dir = new File(uploadDir);
					if (!dir.exists()) {
						boolean created = dir.mkdirs();
						if (!created) {
							logger.error("디렉토리 생성 실패: " + uploadDir);
							throw new IOException("이미지 저장 디렉토리를 생성할 수 없습니다.");
						}
					}
					
					Path filePath = Paths.get(uploadDir + safeFileName);
					Files.write(filePath, profileImage.getBytes());
					
					map.put("profile_image", "/images/" + safeFileName);
					logger.info("이미지 저장 성공: " + safeFileName);
				} catch (IOException e) {
					logger.error("이미지 저장 실패: " + e.getMessage(), e);
					rtnVal.put("returnCode", "FAILURE");
					rtnVal.put("errorMessage", "이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
					return rtnVal;
				}
			}
			
			int val = restService.updateDogById(map);
			rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
			rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
		} catch (Exception e) {
			logger.error("반려견 수정 실패: " + e.getMessage(), e);
			rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.");
		}
		return rtnVal;
	}
	
	@PostMapping("/deleteDogById")
	public HashMap deleteDogById(@RequestBody HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 삭제 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
			int val = restService.deleteDogById(map);
			rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
			rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
		} catch (Exception e) {
			rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
		}
		return rtnVal;
	}	
	
	@GetMapping("/getAllDiaries")
	public HashMap getAllDiaries(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 user_id로만 조회
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId());
			}
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
			
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getAllDiaries(map));  
        } catch (Exception e) {
        	logger.error("일지 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;	
	}	
	
	@PostMapping("/saveDogDiaryByDogId")
	public HashMap saveDogDiaryByDogId(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
	        int val = restService.saveDogDiaryByDogId(map);
	        if (val > 0) {
	        	// 알림 설정 확인 후 알림 생성
	        	try {
	        		HashMap settings = notificationService.getNotificationSettings(loginUser.getId());
	        		if (settings != null && (settings.get("diary_reminder") == Boolean.TRUE || 
	        				(settings.get("diary_reminder") instanceof Number && ((Number)settings.get("diary_reminder")).intValue() == 1))) {
	        			// 강아지 이름 조회
	        			HashMap dogMap = new HashMap();
	        			dogMap.put("user_id", loginUser.getId());
	        			List<HashMap> dogs = restService.getDogsByUserId(dogMap);
	        			String dogName = "반려견";
	        			if (dogs != null && !dogs.isEmpty() && map.get("dog_id") != null) {
	        				for (HashMap dog : dogs) {
	        					if (dog.get("id").toString().equals(map.get("dog_id").toString())) {
	        						dogName = dog.get("name") != null ? dog.get("name").toString() : "반려견";
	        						break;
	        					}
	        				}
	        			}
	        			String mood = map.get("mood") != null ? map.get("mood").toString() : "";
	        			String moodLabel = mood.equals("HAPPY") ? "행복" : mood.equals("SAD") ? "슬픔" : mood.equals("ANGRY") ? "화남" : mood.equals("EXCITED") ? "신남" : "";
	        			notificationService.createNotification(
	        				loginUser.getId(),
	        				"DIARY",
	        				"일지가 작성되었습니다",
	        				dogName + "의 " + moodLabel + " 일지가 작성되었습니다."
	        			);
	        		}
	        	} catch (Exception e) {
	        		logger.warn("알림 생성 실패 (일지): " + e.getMessage());
	        	}
	        }
	        rtnVal.put("returnCode", val == 0 ? "FAILURE" : "SUCCESS");
	        rtnVal.put(val == 0 ? "errorMessage" : "resultData", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
	
	@PostMapping("/updateDogDiaryByDogId")
	public HashMap updateDogDiaryByDogId(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 수정 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
	        int val = restService.updateDogDiaryByDogId(map);
	        rtnVal.put("returnCode", val == 0 ? "FAILURE" : "SUCCESS");
	        rtnVal.put(val == 0 ? "errorMessage" : "resultData", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
	
	@PostMapping("/deleteDogDiaryById")
    public HashMap<String, Object> deleteDogDiaryById(@RequestBody HashMap<String, Object> map) {
		HashMap rtnVal = new HashMap();
        try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 삭제 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
            int val = restService.deleteDogDiaryById(map);
            rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
        } catch (Exception e) {
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }
	
	@GetMapping("/getSchedulesByMonth")
    public HashMap getSchedulesByMonth(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap<>();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 user_id로만 조회
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId());
			}
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
			
	        if (!map.containsKey("month")) {
	            rtnVal.put("returnCode", "FAILURE");
	            rtnVal.put("errorMessage", "month는 필수 파라미터입니다.");
	            return rtnVal;
	        }

	        rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", restService.findSchedulesByMonthAndUser(map));
	    } catch (Exception e) {
	    	logger.error("일정 조회 실패: " + e.getMessage(), e);
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
    }
	
	@GetMapping("/getMonthlyStatistics")
	public HashMap getMonthlyStatistics(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap<>();
		try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 user_id로만 조회
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId());
			}
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
			
			rtnVal.put("returnCode", "SUCCESS");
			rtnVal.put("resultData", restService.getMonthlyStatistics(map));
		} catch (Exception e) {
			logger.error("월별 통계 조회 실패: " + e.getMessage(), e);
			rtnVal.put("returnCode", "FAILURE");
			rtnVal.put("errorMessage", e.getMessage());
		}
		return rtnVal;
	}
	
	@PostMapping("/createWalk")
    public HashMap createWalk(@RequestBody HashMap map) {
		HashMap rtnVal = new HashMap();
        try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
            // 필수 필드 검증
            if (!map.containsKey("dog_id") || !map.containsKey("date")) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "필수 필드(dog_id, date)가 누락되었습니다.");
                return rtnVal;
            }
            
            map.put("user_id", loginUser.getId()); // 로그인한 사용자의 ID로 설정
            int val = restService.insertWalk(map);
            rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
            if (val > 0) {
                // 알림 설정 확인 후 알림 생성
                try {
                    HashMap settings = notificationService.getNotificationSettings(loginUser.getId());
                    if (settings != null && (settings.get("schedule_notification") == Boolean.TRUE || 
                            (settings.get("schedule_notification") instanceof Number && ((Number)settings.get("schedule_notification")).intValue() == 1))) {
                        // 강아지 이름 조회
                        HashMap dogMap = new HashMap();
                        dogMap.put("user_id", loginUser.getId());
                        List<HashMap> dogs = restService.getDogsByUserId(dogMap);
                        String dogName = "반려견";
                        if (dogs != null && !dogs.isEmpty() && map.get("dog_id") != null) {
                            for (HashMap dog : dogs) {
                                if (dog.get("id").toString().equals(map.get("dog_id").toString())) {
                                    dogName = dog.get("name") != null ? dog.get("name").toString() : "반려견";
                                    break;
                                }
                            }
                        }
                        String date = map.get("date") != null ? map.get("date").toString() : "";
                        notificationService.createNotification(
                            loginUser.getId(),
                            "SCHEDULE",
                            "산책 일정이 등록되었습니다",
                            dogName + "의 산책 일정이 " + date + "에 등록되었습니다."
                        );
                    }
                } catch (Exception e) {
                    logger.warn("알림 생성 실패 (산책): " + e.getMessage());
                }
                rtnVal.put("resultData", val);
            } else {
                rtnVal.put("errorMessage", "산책 일정 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }
	
	@PostMapping("/updateWalk")
	public HashMap updateWalk(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 수정 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
	        int val = restService.updateWalk(map);
	        rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
	        rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
	
	@PostMapping("/deleteWalk")
	public HashMap deleteWalk(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 삭제 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
	        int val = restService.deleteWalkById(map);
	        rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
	        rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
	
	@PostMapping("/createHospital")
    public HashMap createHospital(@RequestBody HashMap map) {
		HashMap rtnVal = new HashMap();
        try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
            // 필수 필드 검증
            if (!map.containsKey("dog_id") || !map.containsKey("date")) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "필수 필드(dog_id, date)가 누락되었습니다.");
                return rtnVal;
            }
            
            map.put("user_id", loginUser.getId()); // 로그인한 사용자의 ID로 설정
            int val = restService.insertHospital(map);
            rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
            if (val > 0) {
            	// 알림 설정 확인 후 알림 생성
            	try {
            		HashMap settings = notificationService.getNotificationSettings(loginUser.getId());
            		if (settings != null && (settings.get("schedule_notification") == Boolean.TRUE || 
            				(settings.get("schedule_notification") instanceof Number && ((Number)settings.get("schedule_notification")).intValue() == 1))) {
            			// 강아지 이름 조회
            			HashMap dogMap = new HashMap();
            			dogMap.put("user_id", loginUser.getId());
            			List<HashMap> dogs = restService.getDogsByUserId(dogMap);
            			String dogName = "반려견";
            			if (dogs != null && !dogs.isEmpty() && map.get("dog_id") != null) {
            				for (HashMap dog : dogs) {
            					if (dog.get("id").toString().equals(map.get("dog_id").toString())) {
            						dogName = dog.get("name") != null ? dog.get("name").toString() : "반려견";
            						break;
            					}
            				}
            			}
            			String date = map.get("date") != null ? map.get("date").toString() : "";
            			String hospitalName = map.get("hospital_name") != null ? map.get("hospital_name").toString() : "";
            			notificationService.createNotification(
            				loginUser.getId(),
            				"SCHEDULE",
            				"병원 일정이 등록되었습니다",
            				dogName + "의 병원 일정이 " + date + (hospitalName != null && !hospitalName.isEmpty() ? " (" + hospitalName + ")" : "") + "에 등록되었습니다."
            			);
            		}
            	} catch (Exception e) {
            		logger.warn("알림 생성 실패 (병원): " + e.getMessage());
            	}
                rtnVal.put("resultData", val);
            } else {
                rtnVal.put("errorMessage", "병원 일정 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }
	
	@PostMapping("/updateHospital")
	public HashMap updateHospital(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 수정 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
	        int val = restService.updateHospital(map);
	        rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
	        rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}

	@PostMapping("/deleteHospital")
	public HashMap deleteHospital(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
			// 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "로그인이 필요합니다.");
				return rtnVal;
			}
			
			User loginUser = userService.getUserByName(authentication.getName());
			if (loginUser == null) {
				rtnVal.put("returnCode", "FAILURE");
				rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
				return rtnVal;
			}
			
			// ADMIN이 아닌 경우 자신의 데이터만 삭제 가능
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			
			if (!isAdmin) {
				map.put("user_id", loginUser.getId()); // 권한 검증을 위해 user_id 추가
			}
	        int val = restService.deleteHospitalById(map);
	        rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
	        rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
	
	// 관리자용 전체 산책 조회
	@GetMapping("/admin/getAllWalks")
	public HashMap getAllWalks(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
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
			
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getAllWalks(map));  
        } catch (Exception e) {
        	logger.error("산책 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
	}
	
	// 관리자용 전체 병원 조회
	@GetMapping("/admin/getAllHospitals")
	public HashMap getAllHospitals(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
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
			
			// ADMIN인 경우 user_id가 없으면 모든 데이터 조회, 있으면 해당 user_id의 데이터만 조회
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getAllHospitals(map));  
        } catch (Exception e) {
        	logger.error("병원 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
	}
}
