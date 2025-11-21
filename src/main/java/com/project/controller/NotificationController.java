package com.project.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.model.User;
import com.project.service.NotificationService;
import com.project.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestMapping(value = "/api")
@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 알림 목록 조회
    @GetMapping("/notifications")
    public HashMap getNotifications(@RequestParam(required = false) Integer limit) {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            List<HashMap> notifications = notificationService.getNotificationsByUserId(loginUser.getId(), limit);
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", notifications);
        } catch (Exception e) {
            logger.error("알림 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/notifications/unread-count")
    public HashMap getUnreadCount() {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            int unreadCount = notificationService.getUnreadCountByUserId(loginUser.getId());
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", unreadCount);
        } catch (Exception e) {
            logger.error("읽지 않은 알림 개수 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }

    // 알림 읽음 처리
    @PostMapping("/notifications/mark-read")
    public HashMap markAsRead(@RequestBody HashMap map) {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            Long notificationId = map.get("id") != null ? Long.parseLong(map.get("id").toString()) : null;
            if (notificationId == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "알림 ID가 필요합니다.");
                return rtnVal;
            }

            int result = notificationService.markAsRead(loginUser.getId(), notificationId);
            rtnVal.put("returnCode", result > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put("resultData", result);
        } catch (Exception e) {
            logger.error("알림 읽음 처리 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }

    // 알림 삭제
    @PostMapping("/notifications/delete")
    public HashMap deleteNotification(@RequestBody HashMap map) {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            Long notificationId = map.get("id") != null ? Long.parseLong(map.get("id").toString()) : null;
            if (notificationId == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "알림 ID가 필요합니다.");
                return rtnVal;
            }

            int result = notificationService.deleteNotification(loginUser.getId(), notificationId);
            rtnVal.put("returnCode", result > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put("resultData", result);
        } catch (Exception e) {
            logger.error("알림 삭제 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }

    // 알림 설정 조회
    @GetMapping("/notification-settings")
    public HashMap getNotificationSettings() {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            HashMap settings = notificationService.getNotificationSettings(loginUser.getId());
            rtnVal.put("returnCode", "SUCCESS");
            rtnVal.put("resultData", settings);
        } catch (Exception e) {
            logger.error("알림 설정 조회 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }

    // 알림 설정 저장
    @PostMapping("/notification-settings")
    public HashMap saveNotificationSettings(@RequestBody HashMap map) {
        HashMap rtnVal = new HashMap();
        try {
            // 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "로그인이 필요합니다.");
                return rtnVal;
            }

            User loginUser = userService.getUserByName(authentication.getName());
            if (loginUser == null) {
                rtnVal.put("returnCode", "FAILURE");
                rtnVal.put("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return rtnVal;
            }

            Boolean scheduleNotification = map.get("schedule_notification") != null 
                    ? Boolean.parseBoolean(map.get("schedule_notification").toString()) : true;
            Boolean diaryReminder = map.get("diary_reminder") != null 
                    ? Boolean.parseBoolean(map.get("diary_reminder").toString()) : false;
            String notificationTime = map.get("notification_time") != null 
                    ? map.get("notification_time").toString() : "09:00";

            int result = notificationService.saveNotificationSettings(
                    loginUser.getId(), scheduleNotification, diaryReminder, notificationTime);
            rtnVal.put("returnCode", result > 0 ? "SUCCESS" : "FAILURE");
            rtnVal.put("resultData", result);
        } catch (Exception e) {
            logger.error("알림 설정 저장 실패: " + e.getMessage(), e);
            rtnVal.put("returnCode", "FAILURE");
            rtnVal.put("errorMessage", e.getMessage());
        }
        return rtnVal;
    }
}

