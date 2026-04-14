package com.zxl.rag.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "insurance_case")
public class InsuranceCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true)
    private String caseNumber;

    @Column(name = "insurance_type")
    private String insuranceType;

    @Column(name = "policy_number")
    private String policyNumber;

    @Column(name = "insured_name")
    private String insuredName;

    @Column(name = "insurance_company")
    private String insuranceCompany;

    @Column(name = "case_status")
    private String caseStatus;

    @Column(name = "case_description", columnDefinition = "TEXT")
    private String caseDescription;

    @Column(name = "estimated_amount")
    private Double estimatedAmount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToOne(mappedBy = "insuranceCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CaseDelegate delegate;

    @OneToOne(mappedBy = "insuranceCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CaseAccident accident;

    @OneToOne(mappedBy = "insuranceCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CaseSurvey survey;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getInsuranceType() { return insuranceType; }
    public void setInsuranceType(String insuranceType) { this.insuranceType = insuranceType; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getInsuredName() { return insuredName; }
    public void setInsuredName(String insuredName) { this.insuredName = insuredName; }

    public String getInsuranceCompany() { return insuranceCompany; }
    public void setInsuranceCompany(String insuranceCompany) { this.insuranceCompany = insuranceCompany; }

    public String getCaseStatus() { return caseStatus; }
    public void setCaseStatus(String caseStatus) { this.caseStatus = caseStatus; }

    public String getCaseDescription() { return caseDescription; }
    public void setCaseDescription(String caseDescription) { this.caseDescription = caseDescription; }

    public Double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(Double estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public CaseDelegate getDelegate() { return delegate; }
    public void setDelegate(CaseDelegate delegate) { this.delegate = delegate; }

    public CaseAccident getAccident() { return accident; }
    public void setAccident(CaseAccident accident) { this.accident = accident; }

    public CaseSurvey getSurvey() { return survey; }
    public void setSurvey(CaseSurvey survey) { this.survey = survey; }
}
