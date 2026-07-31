package com.platformcommons.identity.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.enums.MemberStatus;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.domain.Member;
import com.platformcommons.identity.domain.MemberRole;
import com.platformcommons.identity.repository.MemberRepository;
import com.platformcommons.identity.repository.entity.MemberEntity;
import com.platformcommons.identity.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MemberService} 默认实现。
 *
 * <p>负责成员注册、查询、状态变更等核心业务逻辑。
 * 日志统一使用 SLF4J，占位符拼接。</p>
 */
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {


    /** 劳动者初始劳动份额 */
    private static final int INITIAL_LABOR_SHARES = 0;

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberResponse register(MemberRegisterRequest request) {
        log.info("注册成员：name={}, phone={}", request.name(), maskPhone(request.phone()));

        // 手机号唯一性校验
        Optional<MemberEntity> existed = memberRepository.findByPhone(request.phone());
        if (existed.isPresent()) {
            log.warn("手机号已注册：phone={}", maskPhone(request.phone()));
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "手机号已被注册");
        }

        Set<MemberRole> roles = parseRoles(request.roles());
        MemberEntity entity = new MemberEntity();
        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setRoles(serializeRoles(roles));
        entity.setStatus(MemberStatus.ACTIVE.name());
        entity.setRegisteredAt(LocalDateTime.now());
        entity.setLaborShares(hasWorker(roles) ? INITIAL_LABOR_SHARES : null);

        MemberEntity saved = memberRepository.save(entity);
        log.info("注册成功：id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        MemberEntity entity = requireMember(id);
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberResponseById(Long id) {
        return toResponse(requireMember(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers() {
        return memberRepository.findAll().stream()
                .map(MemberServiceImpl::toResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberResponse changeStatus(Long id, String status) {
        log.info("变更成员状态：id={}, targetStatus={}", id, status);
        MemberStatus target = parseStatus(status);
        MemberEntity entity = requireMember(id);
        entity.setStatus(target.name());
        MemberEntity saved = memberRepository.save(entity);
        log.info("状态变更完成：id={}, status={}", id, target);
        return toResponse(saved);
    }

    // ===== 内部工具 =====

    private MemberEntity requireMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "成员不存在: " + id));
    }

    private static boolean hasWorker(Set<MemberRole> roles) {
        return roles.contains(MemberRole.WORKER);
    }

    private static Set<MemberRole> parseRoles(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "角色不能为空");
        }
        return roleNames.stream()
                .map(MemberServiceImpl::parseRole)
                .collect(Collectors.toSet());
    }

    private static MemberRole parseRole(String name) {
        try {
            return MemberRole.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法角色: " + name);
        }
    }

    private static String serializeRoles(Set<MemberRole> roles) {
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    private static Set<MemberRole> deserializeRoles(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(MemberRole::valueOf)
                .collect(Collectors.toSet());
    }

    private static MemberStatus parseStatus(String status) {
        try {
            return MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法状态: " + status);
        }
    }

    private static Member toDomain(MemberEntity entity) {
        return new Member(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                deserializeRoles(entity.getRoles()),
                entity.getRegisteredAt(),
                MemberStatus.valueOf(entity.getStatus()),
                entity.getLaborShares()
        );
    }

    private static MemberResponse toResponse(MemberEntity entity) {
        return new MemberResponse(
                entity.getId(),
                entity.getName(),
                maskPhone(entity.getPhone()),
                deserializeRoles(entity.getRoles()),
                entity.getRegisteredAt(),
                entity.getStatus(),
                entity.getLaborShares()
        );
    }

    /**
     * 手机号脱敏：保留前 3 位与后 4 位。
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
