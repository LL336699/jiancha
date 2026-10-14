package com.jiancha.biz.edu.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.jiancha.biz.edu.domain.JcEduCommitment;
import com.jiancha.biz.edu.mapper.JcEduCommitmentMapper;
import com.jiancha.biz.edu.service.IJcEduCommitmentService;

/**
 * 廉洁承诺书签收服务实现（M9）
 *
 * @author 小标
 */
@Service
public class JcEduCommitmentServiceImpl implements IJcEduCommitmentService {

    /** 状态：草稿/已发布/已关闭 */
    private static final String COMMIT_DRAFT = "0";
    private static final String COMMIT_PUBLISHED = "1";
    private static final String COMMIT_CLOSED = "2";

    /** 适用范围：全员 */
    private static final String SCOPE_ALL = "全员";

    /** recordType：0 承诺书本体 / 1 签收记录 */
    private static final String RECORD_BODY = "0";
    private static final String RECORD_SIGN = "1";

    @Autowired
    private JcEduCommitmentMapper commitmentMapper;

    @Override
    public List<JcEduCommitment> selectCommitmentList(JcEduCommitment query) {
        return commitmentMapper.selectJcEduCommitmentList(query);
    }

    @Override
    public JcEduCommitment selectCommitmentById(Long commitId) {
        return commitmentMapper.selectJcEduCommitmentByCommitId(commitId);
    }

    @Override
    public List<JcEduCommitment> selectPublished() {
        return commitmentMapper.selectPublished();
    }

    @Override
    @Transactional
    public int insertCommitment(JcEduCommitment commitment) {
        if (StringUtils.isEmpty(commitment.getTitle())) {
            throw new ServiceException("承诺书标题不能为空");
        }
        if (StringUtils.isEmpty(commitment.getContent())) {
            throw new ServiceException("承诺书正文不能为空");
        }
        if (StringUtils.isEmpty(commitment.getScopeType())) {
            throw new ServiceException("适用范围不能为空");
        }
        commitment.setStatus(COMMIT_DRAFT);
        commitment.setRecordType(RECORD_BODY);
        commitment.setCreateBy(SecurityUtils.getUsername());
        return commitmentMapper.insertJcEduCommitment(commitment);
    }

    @Override
    @Transactional
    public int updateCommitment(JcEduCommitment commitment) {
        JcEduCommitment exists = commitmentMapper.selectJcEduCommitmentByCommitId(commitment.getCommitId());
        if (exists == null) {
            throw new ServiceException("承诺书不存在");
        }
        if (!COMMIT_DRAFT.equals(exists.getStatus())) {
            throw new ServiceException("仅草稿状态可修改");
        }
        commitment.setUpdateBy(SecurityUtils.getUsername());
        return commitmentMapper.updateJcEduCommitment(commitment);
    }

    @Override
    public int publishCommitment(Long commitId) {
        JcEduCommitment exists = commitmentMapper.selectJcEduCommitmentByCommitId(commitId);
        if (exists == null) {
            throw new ServiceException("承诺书不存在");
        }
        if (!COMMIT_DRAFT.equals(exists.getStatus())) {
            throw new ServiceException("仅草稿状态可发布");
        }
        return commitmentMapper.publishCommitment(commitId, SecurityUtils.getUsername());
    }

    @Override
    public int closeCommitment(Long commitId) {
        return commitmentMapper.closeCommitment(commitId, SecurityUtils.getUsername());
    }

    @Override
    @Transactional
    public int deleteCommitment(Long commitId) {
        JcEduCommitment exists = commitmentMapper.selectJcEduCommitmentByCommitId(commitId);
        if (exists == null) {
            throw new ServiceException("承诺书不存在");
        }
        if (!COMMIT_DRAFT.equals(exists.getStatus())) {
            throw new ServiceException("仅草稿状态可删除");
        }
        return commitmentMapper.deleteJcEduCommitmentByCommitId(commitId);
    }

    @Override
    @Transactional
    public JcEduCommitment sign(Long commitId, Long personId, String personName) {
        if (personId == null) {
            throw new ServiceException("签收人不能为空");
        }
        JcEduCommitment body = commitmentMapper.selectJcEduCommitmentByCommitId(commitId);
        if (body == null || !RECORD_BODY.equals(body.getRecordType())) {
            throw new ServiceException("承诺书不存在");
        }
        if (!COMMIT_PUBLISHED.equals(body.getStatus())) {
            throw new ServiceException("承诺书未发布或已关闭，不能签收");
        }
        // 幂等：已签收返回既有记录
        JcEduCommitment exists = commitmentMapper.selectSignRecord(commitId, personId);
        if (exists != null) {
            return exists;
        }
        JcEduCommitment sign = new JcEduCommitment();
        sign.setParentId(commitId);
        sign.setTitle(body.getTitle());
        sign.setPersonId(personId);
        sign.setPersonName(personName);
        sign.setRecordType(RECORD_SIGN);
        sign.setStatus(COMMIT_PUBLISHED); // 签收记录状态：已签收
        sign.setSignType("电子签收");
        sign.setSignTime(new java.util.Date());
        sign.setCreateBy(SecurityUtils.getUsername());
        commitmentMapper.insertJcEduCommitment(sign);
        return sign;
    }

    @Override
    public Map<String, Object> signProgress(Long commitId) {
        JcEduCommitment body = commitmentMapper.selectJcEduCommitmentByCommitId(commitId);
        if (body == null) {
            throw new ServiceException("承诺书不存在");
        }
        long signed = commitmentMapper.countSigned(commitId);
        long expected = commitmentMapper.countExpected(commitId);
        Map<String, Object> progress = new HashMap<>();
        progress.put("commitId", commitId);
        progress.put("title", body.getTitle());
        progress.put("scopeType", body.getScopeType());
        progress.put("signedCount", signed);
        // 仅全员 scope 可预测应签人数；其他范围由部门自查
        progress.put("expectedCount", SCOPE_ALL.equals(body.getScopeType()) ? expected : null);
        progress.put("expectedPredictable", SCOPE_ALL.equals(body.getScopeType()));
        return progress;
    }
}
