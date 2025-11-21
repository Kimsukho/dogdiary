package com.project.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.dao.RestDao;
import com.project.service.RestService;

@Service
public class RestServiceImpl implements RestService {

	@Autowired
	private RestDao restDao;
	
	@Override
	public List<HashMap> getDogsByUserId(HashMap map) {
		return restDao.getDogsByUserId(map);
	}

	@Override
	public int saveDog(HashMap map) {
		return restDao.saveDog(map);
	}

	@Override
	public int updateDogById(HashMap map) {
		return restDao.updateDogById(map);
	}

	@Override
	public int deleteDogById(HashMap map) {
		return restDao.deleteDogById(map);
	}

	@Override
	public List<HashMap> getAllDiaries(HashMap map) {
		return restDao.getAllDiaries(map);
	}

	@Override
	public int saveDogDiaryByDogId(HashMap map) {
		return restDao.saveDogDiaryByDogId(map);
	}

	@Override
	public int updateDogDiaryByDogId(HashMap map) {
		return restDao.updateDogDiaryByDogId(map);
	}

	@Override
	public int deleteDogDiaryById(HashMap map) {
		return restDao.deleteDogDiaryById(map);
	}

	@Override
	public List<HashMap> findSchedulesByMonthAndUser(HashMap map) {
		return restDao.findSchedulesByMonthAndUser(map);
	}

	@Override
	public int insertWalk(HashMap map) {
		return restDao.insertWalk(map);
	}

	@Override
	public int updateWalk(HashMap map) {
		return restDao.updateWalk(map);
	}

	@Override
	public int deleteWalkById(HashMap map) {
		return restDao.deleteWalkById(map);
	}

	@Override
	public int insertHospital(HashMap map) {
		return restDao.insertHospital(map);
	}

	@Override
	public int updateHospital(HashMap map) {
		return restDao.updateHospital(map);
	}

	@Override
	public int deleteHospitalById(HashMap map) {
		return restDao.deleteHospitalById(map);
	}

	@Override
	public List<HashMap> getMonthlyStatistics(HashMap map) {
		return restDao.getMonthlyStatistics(map);
	}
	
	@Override
	public List<HashMap> getAllWalks(HashMap map) {
		return restDao.getAllWalks(map);
	}
	
	@Override
	public List<HashMap> getAllHospitals(HashMap map) {
		return restDao.getAllHospitals(map);
	}
	
}