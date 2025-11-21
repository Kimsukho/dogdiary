package com.project.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.dao.NotificationDao;
import com.project.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationDao notificationDao;

    @Override
    public List<HashMap> getNotificationsByUserId(Long userId, Integer limit) {
        HashMap map = new HashMap();
        map.put("user_id", userId);
        if (limit != null) {
            map.put("limit", limit);
        }
        return notificationDao.getNotificationsByUserId(map);
    }

    @Override
    public int getUnreadCountByUserId(Long userId) {
        return notificationDao.getUnreadCountByUserId(userId);
    }

    @Override
    public int createNotification(Long userId, String type, String title, String message) {
        HashMap map = new HashMap();
        map.put("user_id", userId);
        map.put("type", type);
        map.put("title", title);
        map.put("message", message);
        return notificationDao.insertNotification(map);
    }

    @Override
    public int markAsRead(Long userId, Long notificationId) {
        HashMap map = new HashMap();
        map.put("id", notificationId);
        map.put("user_id", userId);
        return notificationDao.markAsRead(map);
    }

    @Override
    public int deleteNotification(Long userId, Long notificationId) {
        HashMap map = new HashMap();
        map.put("id", notificationId);
        map.put("user_id", userId);
        return notificationDao.deleteNotification(map);
    }

    @Override
    public HashMap getNotificationSettings(Long userId) {
        return notificationDao.getNotificationSettingsByUserId(userId);
    }

    @Override
    public int saveNotificationSettings(Long userId, Boolean scheduleNotification, Boolean diaryReminder, String notificationTime) {
        HashMap map = new HashMap();
        map.put("user_id", userId);
        map.put("schedule_notification", scheduleNotification);
        map.put("diary_reminder", diaryReminder);
        map.put("notification_time", notificationTime);
        return notificationDao.upsertNotificationSettings(map);
    }
}

