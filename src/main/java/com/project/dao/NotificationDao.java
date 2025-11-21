package com.project.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationDao {
    // 알림 조회
    List<HashMap> getNotificationsByUserId(HashMap map);
    
    // 읽지 않은 알림 개수 조회
    int getUnreadCountByUserId(Long userId);
    
    // 알림 생성
    int insertNotification(HashMap map);
    
    // 알림 읽음 처리
    int markAsRead(HashMap map);
    
    // 알림 삭제
    int deleteNotification(HashMap map);
    
    // 알림 설정 조회
    HashMap getNotificationSettingsByUserId(Long userId);
    
    // 알림 설정 저장/업데이트
    int upsertNotificationSettings(HashMap map);
}

