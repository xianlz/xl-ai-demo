package com.zxl.rag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReportSection {

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "section_prompt", columnDefinition = "TEXT")
    private String sectionPrompt;

    @Column(name = "section_order")
    private Integer order;

    @Column(name = "required")
    private Boolean required;

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getSectionPrompt() { return sectionPrompt; }
    public void setSectionPrompt(String sectionPrompt) { this.sectionPrompt = sectionPrompt; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
}
