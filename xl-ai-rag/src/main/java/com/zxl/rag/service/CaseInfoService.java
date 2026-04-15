package com.zxl.rag.service;

import com.zxl.rag.entity.*;
import com.zxl.rag.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 案件信息服务
 */
@Service
public class CaseInfoService {

    private static final Logger logger = LoggerFactory.getLogger(CaseInfoService.class);

    private final InsuranceCaseRepository caseRepository;
    private final CaseDelegateRepository delegateRepository;
    private final CaseAccidentRepository accidentRepository;
    private final CaseSurveyRepository surveyRepository;

    public CaseInfoService(
            InsuranceCaseRepository caseRepository,
            CaseDelegateRepository delegateRepository,
            CaseAccidentRepository accidentRepository,
            CaseSurveyRepository surveyRepository) {
        this.caseRepository = caseRepository;
        this.delegateRepository = delegateRepository;
        this.accidentRepository = accidentRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * 创建案件（包含所有相关信息）
     */
    @Transactional
    public InsuranceCase createCase(
            InsuranceCase insuranceCase,
            CaseDelegate delegate,
            CaseAccident accident,
            CaseSurvey survey) {

        // 保存案件基础信息
        InsuranceCase savedCase = caseRepository.save(insuranceCase);

        // 保存委托信息
        if (delegate != null) {
            delegate.setInsuranceCase(savedCase);
            delegateRepository.save(delegate);
        }

        // 保存出险信息
        if (accident != null) {
            accident.setInsuranceCase(savedCase);
            accidentRepository.save(accident);
        }

        // 保存查勘信息
        if (survey != null) {
            survey.setInsuranceCase(savedCase);
            surveyRepository.save(survey);
        }

        logger.info("案件创建成功: {}", savedCase.getCaseNumber());
        return savedCase;
    }

    /**
     * 更新案件
     */
    @Transactional
    public InsuranceCase updateCase(
            Long caseId,
            InsuranceCase insuranceCase,
            CaseDelegate delegate,
            CaseAccident accident,
            CaseSurvey survey) {

        InsuranceCase existingCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("案件不存在: " + caseId));

        // 更新基础信息
        if (insuranceCase != null) {
            existingCase.setInsuranceType(insuranceCase.getInsuranceType());
            existingCase.setPolicyNumber(insuranceCase.getPolicyNumber());
            existingCase.setInsuredName(insuranceCase.getInsuredName());
            existingCase.setInsuranceCompany(insuranceCase.getInsuranceCompany());
            existingCase.setCaseStatus(insuranceCase.getCaseStatus());
            existingCase.setCaseDescription(insuranceCase.getCaseDescription());
            existingCase.setEstimatedAmount(insuranceCase.getEstimatedAmount());
        }

        // 更新委托信息
        if (delegate != null) {
            Optional<CaseDelegate> existingDelegate = delegateRepository.findByInsuranceCaseId(caseId);
            if (existingDelegate.isPresent()) {
                CaseDelegate d = existingDelegate.get();
                d.setDelegatorName(delegate.getDelegatorName());
                d.setDelegatorPhone(delegate.getDelegatorPhone());
                d.setDelegatorAddress(delegate.getDelegatorAddress());
                d.setDelegateCompany(delegate.getDelegateCompany());
                d.setDelegateDate(delegate.getDelegateDate());
                d.setDelegateType(delegate.getDelegateType());
                d.setRemarks(delegate.getRemarks());
                delegateRepository.save(d);
            } else {
                delegate.setInsuranceCase(existingCase);
                delegateRepository.save(delegate);
            }
        }

        // 更新出险信息
        if (accident != null) {
            Optional<CaseAccident> existingAccident = accidentRepository.findByInsuranceCaseId(caseId);
            if (existingAccident.isPresent()) {
                CaseAccident a = existingAccident.get();
                a.setAccidentTime(accident.getAccidentTime());
                a.setAccidentLocation(accident.getAccidentLocation());
                a.setAccidentType(accident.getAccidentType());
                a.setAccidentDescription(accident.getAccidentDescription());
                a.setDamageSummary(accident.getDamageSummary());
                a.setIsThirdPartyInvolved(accident.getIsThirdPartyInvolved());
                a.setThirdPartyInfo(accident.getThirdPartyInfo());
                a.setPoliceReport(accident.getPoliceReport());
                a.setEmergencyMeasures(accident.getEmergencyMeasures());
                accidentRepository.save(a);
            } else {
                accident.setInsuranceCase(existingCase);
                accidentRepository.save(accident);
            }
        }

        // 更新查勘信息
        if (survey != null) {
            Optional<CaseSurvey> existingSurvey = surveyRepository.findByInsuranceCaseId(caseId);
            if (existingSurvey.isPresent()) {
                CaseSurvey s = existingSurvey.get();
                s.setSurveyorName(survey.getSurveyorName());
                s.setSurveyDate(survey.getSurveyDate());
                s.setSurveyLocation(survey.getSurveyLocation());
                s.setSurveyMethod(survey.getSurveyMethod());
                s.setSurveyResult(survey.getSurveyResult());
                s.setDamageAssessment(survey.getDamageAssessment());
                s.setLossAmount(survey.getLossAmount());
                s.setCompensationOpinion(survey.getCompensationOpinion());
                s.setSurveyPhotos(survey.getSurveyPhotos());
                s.setRemarks(survey.getRemarks());
                surveyRepository.save(s);
            } else {
                survey.setInsuranceCase(existingCase);
                surveyRepository.save(survey);
            }
        }

        return caseRepository.save(existingCase);
    }

    /**
     * 获取案件详情
     */
    public Map<String, Object> getCaseDetail(Long caseId) {
        InsuranceCase insuranceCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("案件不存在: " + caseId));

        Map<String, Object> result = new HashMap<>();
        result.put("case", insuranceCase);

        // 获取委托信息
        delegateRepository.findByInsuranceCaseId(caseId)
                .ifPresent(d -> result.put("delegate", d));

        // 获取出险信息
        accidentRepository.findByInsuranceCaseId(caseId)
                .ifPresent(a -> result.put("accident", a));

        // 获取查勘信息
        surveyRepository.findByInsuranceCaseId(caseId)
                .ifPresent(s -> result.put("survey", s));

        return result;
    }

    /**
     * 获取案件完整信息（用于报告生成）
     */
    public String getCaseSummary(Long caseId) {
        Map<String, Object> detail = getCaseDetail(caseId);

        StringBuilder summary = new StringBuilder();
        InsuranceCase c = (InsuranceCase) detail.get("case");
        summary.append("【案件基础信息】\n");
        summary.append("案件编号: ").append(c.getCaseNumber()).append("\n");
        summary.append("险种类型: ").append(c.getInsuranceType()).append("\n");
        summary.append("保单号: ").append(c.getPolicyNumber()).append("\n");
        summary.append("被保险人: ").append(c.getInsuredName()).append("\n");
        summary.append("保险公司: ").append(c.getInsuranceCompany()).append("\n");
        summary.append("案件状态: ").append(c.getCaseStatus()).append("\n");
        summary.append("预估金额: ").append(c.getEstimatedAmount()).append("\n");
        summary.append("案件描述: ").append(c.getCaseDescription()).append("\n\n");

        if (detail.containsKey("delegate")) {
            CaseDelegate d = (CaseDelegate) detail.get("delegate");
            summary.append("【委托信息】\n");
            summary.append("委托人: ").append(d.getDelegatorName()).append("\n");
            summary.append("委托公司: ").append(d.getDelegateCompany()).append("\n");
            summary.append("委托类型: ").append(d.getDelegateType()).append("\n");
            summary.append("委托日期: ").append(d.getDelegateDate()).append("\n\n");
        }

        if (detail.containsKey("accident")) {
            CaseAccident a = (CaseAccident) detail.get("accident");
            summary.append("【出险信息】\n");
            summary.append("出险时间: ").append(a.getAccidentTime()).append("\n");
            summary.append("出险地点: ").append(a.getAccidentLocation()).append("\n");
            summary.append("出险类型: ").append(a.getAccidentType()).append("\n");
            summary.append("出险描述: ").append(a.getAccidentDescription()).append("\n");
            summary.append("损失概要: ").append(a.getDamageSummary()).append("\n");
            summary.append("是否有第三方: ").append(a.getIsThirdPartyInvolved()).append("\n");
            if (a.getThirdPartyInfo() != null) {
                summary.append("第三方信息: ").append(a.getThirdPartyInfo()).append("\n");
            }
            summary.append("警方报告: ").append(a.getPoliceReport()).append("\n");
            summary.append("应急措施: ").append(a.getEmergencyMeasures()).append("\n\n");
        }

        if (detail.containsKey("survey")) {
            CaseSurvey s = (CaseSurvey) detail.get("survey");
            summary.append("【查勘信息】\n");
            summary.append("查勘人: ").append(s.getSurveyorName()).append("\n");
            summary.append("查勘日期: ").append(s.getSurveyDate()).append("\n");
            summary.append("查勘地点: ").append(s.getSurveyLocation()).append("\n");
            summary.append("查勘方式: ").append(s.getSurveyMethod()).append("\n");
            summary.append("查勘结果: ").append(s.getSurveyResult()).append("\n");
            summary.append("损失评估: ").append(s.getDamageAssessment()).append("\n");
            summary.append("损失金额: ").append(s.getLossAmount()).append("\n");
            summary.append("理赔意见: ").append(s.getCompensationOpinion()).append("\n\n");
        }

        return summary.toString();
    }

    /**
     * 分页查询案件
     */
    public List<InsuranceCase> listCases(int page, int size) {
        return caseRepository.findAll()
                .stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    /**
     * 根据险种类型查询
     */
    public List<InsuranceCase> findByInsuranceType(String insuranceType) {
        return caseRepository.findAll()
                .stream()
                .filter(c -> insuranceType.equals(c.getInsuranceType()))
                .toList();
    }

    /**
     * 删除案件
     */
    @Transactional
    public void deleteCase(Long caseId) {
        caseRepository.deleteById(caseId);
        logger.info("案件删除成功: {}", caseId);
    }
}
