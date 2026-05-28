package com.wyx.enrollment_promotion_systemmaster.dto;

public class EnrollmentDTO {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private Long userId;
    private String userName;
    private String realName;
    private Integer role;
    private String enrollmentData;
    private String attachments;
    private Integer approvalStatus;
    private Integer currentFlowNode;
    private String groupName;
    private Integer groupRank;
    private String enrollmentTime;
    private String updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getEnrollmentData() {
        return enrollmentData;
    }

    public void setEnrollmentData(String enrollmentData) {
        this.enrollmentData = enrollmentData;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Integer getCurrentFlowNode() {
        return currentFlowNode;
    }

    public void setCurrentFlowNode(Integer currentFlowNode) {
        this.currentFlowNode = currentFlowNode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getGroupRank() {
        return groupRank;
    }

    public void setGroupRank(Integer groupRank) {
        this.groupRank = groupRank;
    }

    public String getEnrollmentTime() {
        return enrollmentTime;
    }

    public void setEnrollmentTime(String enrollmentTime) {
        this.enrollmentTime = enrollmentTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
