package com.project.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
