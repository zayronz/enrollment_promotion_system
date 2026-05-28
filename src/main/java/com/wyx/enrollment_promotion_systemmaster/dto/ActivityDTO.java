package com.wyx.enrollment_promotion_systemmaster.dto;

public class ActivityDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private Integer activityType;
    private Integer activityMode;
    private String startTime;
    private String endTime;
    private String registrationStartTime;
    private String registrationEndTime;
    private String targetUsers;
    private Integer maxStudents;
    private Integer maxTeachers;
    private String approvalFlow;
    private String customFields;
    private String bannerImages;
    private String attachments;
    private Integer status;
    private Long creatorId;
    private String creatorName;
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getActivityType() {
        return activityType;
    }

    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
    }

    public Integer getActivityMode() {
        return activityMode;
    }

    public void setActivityMode(Integer activityMode) {
        this.activityMode = activityMode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(String registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public String getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(String registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public String getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(String targetUsers) {
        this.targetUsers = targetUsers;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Integer getMaxTeachers() {
        return maxTeachers;
    }

    public void setMaxTeachers(Integer maxTeachers) {
        this.maxTeachers = maxTeachers;
    }

    public String getApprovalFlow() {
        return approvalFlow;
    }

    public void setApprovalFlow(String approvalFlow) {
        this.approvalFlow = approvalFlow;
    }

    public String getCustomFields() {
        return customFields;
    }

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public String getBannerImages() {
        return bannerImages;
    }

    public void setBannerImages(String bannerImages) {
        this.bannerImages = bannerImages;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
