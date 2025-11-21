package com.project.model;

import java.sql.Time;
import java.sql.Timestamp;

public class NotificationSettings {
    private Long id;
    private Long user_id;
    private Boolean schedule_notification;
    private Boolean diary_reminder;
    private Time notification_time;
    private Timestamp created_at;
    private Timestamp updated_at;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Boolean getSchedule_notification() {
        return schedule_notification;
    }

    public void setSchedule_notification(Boolean schedule_notification) {
        this.schedule_notification = schedule_notification;
    }

    public Boolean getDiary_reminder() {
        return diary_reminder;
    }

    public void setDiary_reminder(Boolean diary_reminder) {
        this.diary_reminder = diary_reminder;
    }

    public Time getNotification_time() {
        return notification_time;
    }

    public void setNotification_time(Time notification_time) {
        this.notification_time = notification_time;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }
}

