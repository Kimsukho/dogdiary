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
}
