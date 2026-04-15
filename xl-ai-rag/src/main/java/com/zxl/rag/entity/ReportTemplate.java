package com.zxl.rag.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "report_template")
public class ReportTemplate {

    @Id
    @Column(name = "template_id")
    private String templateId;

    @Column(name = "name")
    private String name;

    @Column(name = "template_type")
    private String templateType;

    @Column(name = "description")
    private String description;

    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;

    @Column(name = "docx_template_path")
    private String docxTemplatePath;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "active")
    private Boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_section", joinColumns = @JoinColumn(name = "template_id"))
    private List<ReportSection> sections;

    // Getters and Setters
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPromptTemplate() { return promptTemplate; }
    public void setPromptTemplate(String promptTemplate) { this.promptTemplate = promptTemplate; }

    public String getDocxTemplatePath() { return docxTemplatePath; }
    public void setDocxTemplatePath(String docxTemplatePath) { this.docxTemplatePath = docxTemplatePath; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<ReportSection> getSections() { return sections; }
    public void setSections(List<ReportSection> sections) { this.sections = sections; }
}
