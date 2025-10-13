package com.project.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;


public interface RestService {
	List<HashMap> getDogsByUserId(HashMap map);
	List<HashMap> getAllDiaries(HashMap map);
	int saveDogDiaryByDogId(HashMap map);
	int updateDogDiaryByDogId(HashMap map);
	int deleteDogDiaryById(HashMap map);
}
