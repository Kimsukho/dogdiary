package com.project.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.service.RestService;

@RequestMapping(value = "/api")
@RestController
public class DiaryController {

	@Autowired
	private RestService restService;
	
	@GetMapping("/getDogsByUserId")
	public HashMap getDogsByUserId(@RequestParam HashMap map) {
		HashMap rtnVal = new HashMap();
		try {
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getDogsByUserId(map));  
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
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", restService.getAllDiaries(map));  
        } catch (Exception e) {
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;	
	}	
	
	@PostMapping("/saveDogDiaryByDogId")
	public HashMap saveDogDiaryByDogId(@RequestBody HashMap map) {
	    HashMap rtnVal = new HashMap();
	    try {
	        int val = restService.saveDogDiaryByDogId(map);
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
	        if (!map.containsKey("user_id") || !map.containsKey("month")) {
	            rtnVal.put("returnCode", "FAILURE");
	            rtnVal.put("errorMessage", "userId와 month는 필수 파라미터입니다.");
	            return rtnVal;
	        }

	        rtnVal.put("returnCode", "SUCCESS");
	        rtnVal.put("resultData", restService.findSchedulesByMonthAndUser(map));
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
    }
	
	@PostMapping("/createWalk")
    public HashMap createWalk(@RequestBody HashMap map) {
		HashMap rtnVal = new HashMap();
        try {
            int val = restService.insertWalk(map);
            System.out.println("------------"+ val);
            rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
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
            int val = restService.insertHospital(map);
            rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
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
	        int val = restService.deleteHospitalById(map);
	        rtnVal.put("returnCode", val > 0 ? "SUCCESS" : "FAILURE");
	        rtnVal.put(val > 0 ? "resultData" : "errorMessage", val);
	    } catch (Exception e) {
	        rtnVal.put("returnCode", "FAILURE");
	        rtnVal.put("errorMessage", e.getMessage());
	    }
	    return rtnVal;
	}
}
