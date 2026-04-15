package com.zxl.rag.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_delegate")
public class CaseDelegate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private InsuranceCase insuranceCase;

    @Column(name = "delegator_name")
    private String delegatorName;

    @Column(name = "delegator_phone")
    private String delegatorPhone;

    @Column(name = "delegator_address")
    private String delegatorAddress;

    @Column(name = "delegate_company")
    private String delegateCompany;

    @Column(name = "delegate_date")
    private LocalDateTime delegateDate;

    @Column(name = "delegate_type")
    private String delegateType;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (delegateDate == null) {
            delegateDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InsuranceCase getInsuranceCase() { return insuranceCase; }
    public void setInsuranceCase(InsuranceCase insuranceCase) { this.insuranceCase = insuranceCase; }

    public String getDelegatorName() { return delegatorName; }
    public void setDelegatorName(String delegatorName) { this.delegatorName = delegatorName; }

    public String getDelegatorPhone() { return delegatorPhone; }
    public void setDelegatorPhone(String delegatorPhone) { this.delegatorPhone = delegatorPhone; }

    public String getDelegatorAddress() { return delegatorAddress; }
    public void setDelegatorAddress(String delegatorAddress) { this.delegatorAddress = delegatorAddress; }

    public String getDelegateCompany() { return delegateCompany; }
    public void setDelegateCompany(String delegateCompany) { this.delegateCompany = delegateCompany; }

    public LocalDateTime getDelegateDate() { return delegateDate; }
    public void setDelegateDate(LocalDateTime delegateDate) { this.delegateDate = delegateDate; }

    public String getDelegateType() { return delegateType; }
    public void setDelegateType(String delegateType) { this.delegateType = delegateType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
