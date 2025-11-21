package com.project.service;

import java.util.HashMap;
import java.util.List;

public interface NotificationService {
    // 알림 조회
    List<HashMap> getNotificationsByUserId(Long userId, Integer limit);
    
    // 읽지 않은 알림 개수 조회
    int getUnreadCountByUserId(Long userId);
    
    // 알림 생성
    int createNotification(Long userId, String type, String title, String message);
    
    // 알림 읽음 처리
    int markAsRead(Long userId, Long notificationId);
    
    // 알림 삭제
    int deleteNotification(Long userId, Long notificationId);
    
    // 알림 설정 조회
    HashMap getNotificationSettings(Long userId);
    
    // 알림 설정 저장
    int saveNotificationSettings(Long userId, Boolean scheduleNotification, Boolean diaryReminder, String notificationTime);
}

