package com.project.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestDao {
	List<HashMap> getDogsByUserId(HashMap map);
	int saveDog(HashMap map);
	int updateDogById(HashMap map);
	int deleteDogById(HashMap map);
	
	List<HashMap> getAllDiaries(HashMap map);
	int saveDogDiaryByDogId(HashMap map);
	int updateDogDiaryByDogId(HashMap map);
	int deleteDogDiaryById(HashMap map);
	List<HashMap> findSchedulesByMonthAndUser(HashMap map);
	
	int insertWalk(HashMap map);
	int updateWalk(HashMap map);
	int deleteWalkById(HashMap map);

	int insertHospital(HashMap map);
	int updateHospital(HashMap map);
	int deleteHospitalById(HashMap map);
	
	List<HashMap> getMonthlyStatistics(HashMap map);
	
	// 관리자용 전체 조회
	List<HashMap> getAllWalks(HashMap map);
	List<HashMap> getAllHospitals(HashMap map);
}