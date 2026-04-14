package com.zxl.rag.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "case_accident")
public class CaseAccident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private InsuranceCase insuranceCase;

    @Column(name = "accident_time")
    private LocalDateTime accidentTime;

    @Column(name = "accident_location")
    private String accidentLocation;

    @Column(name = "accident_type")
    private String accidentType;

    @Column(name = "accident_description", columnDefinition = "TEXT")
    private String accidentDescription;

    @Column(name = "damage_summary", columnDefinition = "TEXT")
    private String damageSummary;

    @Column(name = "is_third_party_involved")
    private Boolean isThirdPartyInvolved;

    @Column(name = "third_party_info")
    private String thirdPartyInfo;

    @Column(name = "police_report")
    private String policeReport;

    @Column(name = "emergency_measures", columnDefinition = "TEXT")
    private String emergencyMeasures;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InsuranceCase getInsuranceCase() { return insuranceCase; }
    public void setInsuranceCase(InsuranceCase insuranceCase) { this.insuranceCase = insuranceCase; }

    public LocalDateTime getAccidentTime() { return accidentTime; }
    public void setAccidentTime(LocalDateTime accidentTime) { this.accidentTime = accidentTime; }

    public String getAccidentLocation() { return accidentLocation; }
    public void setAccidentLocation(String accidentLocation) { this.accidentLocation = accidentLocation; }

    public String getAccidentType() { return accidentType; }
    public void setAccidentType(String accidentType) { this.accidentType = accidentType; }

    public String getAccidentDescription() { return accidentDescription; }
    public void setAccidentDescription(String accidentDescription) { this.accidentDescription = accidentDescription; }

    public String getDamageSummary() { return damageSummary; }
    public void setDamageSummary(String damageSummary) { this.damageSummary = damageSummary; }

    public Boolean getIsThirdPartyInvolved() { return isThirdPartyInvolved; }
    public void setIsThirdPartyInvolved(Boolean isThirdPartyInvolved) { this.isThirdPartyInvolved = isThirdPartyInvolved; }

    public String getThirdPartyInfo() { return thirdPartyInfo; }
    public void setThirdPartyInfo(String thirdPartyInfo) { this.thirdPartyInfo = thirdPartyInfo; }

    public String getPoliceReport() { return policeReport; }
    public void setPoliceReport(String policeReport) { this.policeReport = policeReport; }

    public String getEmergencyMeasures() { return emergencyMeasures; }
    public void setEmergencyMeasures(String emergencyMeasures) { this.emergencyMeasures = emergencyMeasures; }
}
