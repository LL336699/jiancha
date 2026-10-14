package com.jiancha.biz.petition.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 舞弊举报补充材料 jc_petition_material
 *
 * <p>两类上传入口：举报人凭回执码补充材料（免登录）；管理端办理过程中补充证据材料。
 * 文件按 RuoYi 通用上传规范落盘（路径统一走 {@code /profile/upload} 配置）。</p>
 *
 * @author 小标
 */
public class JcPetitionMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 材料ID */
    private Long materialId;

    /** 举报ID */
    private Long petitionId;

    /** 原始文件名 */
    private String fileName;

    /** 落盘路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传方（0举报人 1管理端） */
    private String uploaderType;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploaderType() {
        return uploaderType;
    }

    public void setUploaderType(String uploaderType) {
        this.uploaderType = uploaderType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
