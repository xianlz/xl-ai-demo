package com.zxl.rag.controller;

import com.zxl.rag.entity.*;
import com.zxl.rag.service.CaseInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 案件信息管理控制器
 */
@RestController
@RequestMapping("/api/case")
public class CaseInfoController {

    private final CaseInfoService caseInfoService;

    public CaseInfoController(CaseInfoService caseInfoService) {
        this.caseInfoService = caseInfoService;
    }

    /**
     * 创建案件
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCase(
            @RequestBody Map<String, Object> request) {

        InsuranceCase insuranceCase = new InsuranceCase();
        insuranceCase.setCaseNumber((String) request.get("caseNumber"));
        insuranceCase.setInsuranceType((String) request.get("insuranceType"));
        insuranceCase.setPolicyNumber((String) request.get("policyNumber"));
        insuranceCase.setInsuredName((String) request.get("insuredName"));
        insuranceCase.setInsuranceCompany((String) request.get("insuranceCompany"));
        insuranceCase.setCaseStatus((String) request.get("caseStatus"));
        insuranceCase.setCaseDescription((String) request.get("caseDescription"));
        if (request.get("estimatedAmount") != null) {
            insuranceCase.setEstimatedAmount(((Number) request.get("estimatedAmount")).doubleValue());
        }

        CaseDelegate delegate = parseDelegate(request);
        CaseAccident accident = parseAccident(request);
        CaseSurvey survey = parseSurvey(request);

        InsuranceCase created = caseInfoService.createCase(insuranceCase, delegate, accident, survey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("caseId", created.getId());
        response.put("caseNumber", created.getCaseNumber());

        return ResponseEntity.ok(response);
    }

    /**
     * 更新案件
     */
    @PutMapping("/{caseId}")
    public ResponseEntity<Map<String, Object>> updateCase(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request) {

        InsuranceCase insuranceCase = new InsuranceCase();
        if (request.get("insuranceType") != null) {
            insuranceCase.setInsuranceType((String) request.get("insuranceType"));
        }
        if (request.get("policyNumber") != null) {
            insuranceCase.setPolicyNumber((String) request.get("policyNumber"));
        }
        if (request.get("insuredName") != null) {
            insuranceCase.setInsuredName((String) request.get("insuredName"));
        }
        if (request.get("insuranceCompany") != null) {
            insuranceCase.setInsuranceCompany((String) request.get("insuranceCompany"));
        }
        if (request.get("caseStatus") != null) {
            insuranceCase.setCaseStatus((String) request.get("caseStatus"));
        }
        if (request.get("caseDescription") != null) {
            insuranceCase.setCaseDescription((String) request.get("caseDescription"));
        }
        if (request.get("estimatedAmount") != null) {
            insuranceCase.setEstimatedAmount(((Number) request.get("estimatedAmount")).doubleValue());
        }

        CaseDelegate delegate = parseDelegate(request);
        CaseAccident accident = parseAccident(request);
        CaseSurvey survey = parseSurvey(request);

        caseInfoService.updateCase(caseId, insuranceCase, delegate, accident, survey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "案件更新成功");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取案件详情
     */
    @GetMapping("/{caseId}")
    public ResponseEntity<Map<String, Object>> getCaseDetail(@PathVariable Long caseId) {
        try {
            Map<String, Object> detail = caseInfoService.getCaseDetail(caseId);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取案件摘要（用于报告生成）
     */
    @GetMapping("/{caseId}/summary")
    public ResponseEntity<Map<String, Object>> getCaseSummary(@PathVariable Long caseId) {
        try {
            String summary = caseInfoService.getCaseSummary(caseId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 分页查询案件列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<InsuranceCase> cases = caseInfoService.listCases(page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cases", cases);
        response.put("page", page);
        response.put("size", size);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除案件
     */
    @DeleteMapping("/{caseId}")
    public ResponseEntity<Map<String, Object>> deleteCase(@PathVariable Long caseId) {
        caseInfoService.deleteCase(caseId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "案件删除成功");
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private CaseDelegate parseDelegate(Map<String, Object> request) {
        Map<String, Object> delegateData = (Map<String, Object>) request.get("delegate");
        if (delegateData == null) return null;

        CaseDelegate delegate = new CaseDelegate();
        delegate.setDelegatorName((String) delegateData.get("delegatorName"));
        delegate.setDelegatorPhone((String) delegateData.get("delegatorPhone"));
        delegate.setDelegatorAddress((String) delegateData.get("delegatorAddress"));
        delegate.setDelegateCompany((String) delegateData.get("delegateCompany"));
        delegate.setDelegateType((String) delegateData.get("delegateType"));
        delegate.setRemarks((String) delegateData.get("remarks"));
        return delegate;
    }

    @SuppressWarnings("unchecked")
    private CaseAccident parseAccident(Map<String, Object> request) {
        Map<String, Object> accidentData = (Map<String, Object>) request.get("accident");
        if (accidentData == null) return null;

        CaseAccident accident = new CaseAccident();
        accident.setAccidentLocation((String) accidentData.get("accidentLocation"));
        accident.setAccidentType((String) accidentData.get("accidentType"));
        accident.setAccidentDescription((String) accidentData.get("accidentDescription"));
        accident.setDamageSummary((String) accidentData.get("damageSummary"));
        accident.setIsThirdPartyInvolved((Boolean) accidentData.get("isThirdPartyInvolved"));
        accident.setThirdPartyInfo((String) accidentData.get("thirdPartyInfo"));
        accident.setPoliceReport((String) accidentData.get("policeReport"));
        accident.setEmergencyMeasures((String) accidentData.get("emergencyMeasures"));
        return accident;
    }

    @SuppressWarnings("unchecked")
    private CaseSurvey parseSurvey(Map<String, Object> request) {
        Map<String, Object> surveyData = (Map<String, Object>) request.get("survey");
        if (surveyData == null) return null;

        CaseSurvey survey = new CaseSurvey();
        survey.setSurveyorName((String) surveyData.get("surveyorName"));
        survey.setSurveyLocation((String) surveyData.get("surveyLocation"));
        survey.setSurveyMethod((String) surveyData.get("surveyMethod"));
        survey.setSurveyResult((String) surveyData.get("surveyResult"));
        survey.setDamageAssessment((String) surveyData.get("damageAssessment"));
        if (surveyData.get("lossAmount") != null) {
            survey.setLossAmount(((Number) surveyData.get("lossAmount")).doubleValue());
        }
        survey.setCompensationOpinion((String) surveyData.get("compensationOpinion"));
        survey.setSurveyPhotos((String) surveyData.get("surveyPhotos"));
        survey.setRemarks((String) surveyData.get("remarks"));
        return survey;
    }
}
