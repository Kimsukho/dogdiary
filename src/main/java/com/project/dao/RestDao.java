package com.project.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestDao {
	List<HashMap> getDogsByUserId(HashMap map);
	List<HashMap> getAllDiaries(HashMap map);
	int saveDogDiaryByDogId(HashMap map);
	int updateDogDiaryByDogId(HashMap map);
	int deleteDogDiaryById(HashMap map);
}