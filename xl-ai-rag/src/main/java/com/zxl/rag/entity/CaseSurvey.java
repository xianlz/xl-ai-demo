package com.zxl.rag.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_survey")
public class CaseSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private InsuranceCase insuranceCase;

    @Column(name = "surveyor_name")
    private String surveyorName;

    @Column(name = "survey_date")
    private LocalDateTime surveyDate;

    @Column(name = "survey_location")
    private String surveyLocation;

    @Column(name = "survey_method")
    private String surveyMethod;

    @Column(name = "survey_result", columnDefinition = "TEXT")
    private String surveyResult;

    @Column(name = "damage_assessment", columnDefinition = "TEXT")
    private String damageAssessment;

    @Column(name = "loss_amount")
    private Double lossAmount;

    @Column(name = "compensation_opinion", columnDefinition = "TEXT")
    private String compensationOpinion;

    @Column(name = "survey_photos", columnDefinition = "TEXT")
    private String surveyPhotos;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InsuranceCase getInsuranceCase() { return insuranceCase; }
    public void setInsuranceCase(InsuranceCase insuranceCase) { this.insuranceCase = insuranceCase; }

    public String getSurveyorName() { return surveyorName; }
    public void setSurveyorName(String surveyorName) { this.surveyorName = surveyorName; }

    public LocalDateTime getSurveyDate() { return surveyDate; }
    public void setSurveyDate(LocalDateTime surveyDate) { this.surveyDate = surveyDate; }

    public String getSurveyLocation() { return surveyLocation; }
    public void setSurveyLocation(String surveyLocation) { this.surveyLocation = surveyLocation; }

    public String getSurveyMethod() { return surveyMethod; }
    public void setSurveyMethod(String surveyMethod) { this.surveyMethod = surveyMethod; }

    public String getSurveyResult() { return surveyResult; }
    public void setSurveyResult(String surveyResult) { this.surveyResult = surveyResult; }

    public String getDamageAssessment() { return damageAssessment; }
    public void setDamageAssessment(String damageAssessment) { this.damageAssessment = damageAssessment; }

    public Double getLossAmount() { return lossAmount; }
    public void setLossAmount(Double lossAmount) { this.lossAmount = lossAmount; }

    public String getCompensationOpinion() { return compensationOpinion; }
    public void setCompensationOpinion(String compensationOpinion) { this.compensationOpinion = compensationOpinion; }

    public String getSurveyPhotos() { return surveyPhotos; }
    public void setSurveyPhotos(String surveyPhotos) { this.surveyPhotos = surveyPhotos; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
