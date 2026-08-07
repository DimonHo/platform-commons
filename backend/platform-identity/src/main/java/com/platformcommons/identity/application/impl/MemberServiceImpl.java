package com.platformcommons.identity.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.enums.MemberStatus;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.mask.MaskType;
import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.application.MemberService;
import com.platformcommons.identity.domain.member.Member;
import com.platformcommons.identity.domain.member.MemberEntity;
import com.platformcommons.identity.domain.member.MemberRepository;
import com.platformcommons.identity.domain.role.MemberRole;
import com.platformcommons.identity.domain.role.MemberRoleEntity;
import com.platformcommons.identity.domain.role.MemberRoleRepository;
import com.platformcommons.identity.domain.role.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link MemberService} 默认实现。
 *
 * <p>负责成员注册、查询、状态变更等核心业务逻辑。
 * 日志统一使用 SLF4J，占位符拼接。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberResponse register(MemberRegisterRequest request) {
        log.info("注册成员：name={}, phone={}", request.name(), MaskType.PHONE.mask(request.phone()));

        // 手机号唯一性校验
        Optional<MemberEntity> existed = memberRepository.findByPhone(request.phone());
        if (existed.isPresent()) {
            log.warn("手机号已注册：phone={}", MaskType.PHONE.mask(request.phone()));
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "手机号已被注册");
        }

        MemberEntity entity = new MemberEntity();
        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setStatus(MemberStatus.ACTIVE);
        entity.setRegisteredAt(LocalDateTime.now());

        MemberEntity saved = memberRepository.save(entity);
        log.info("注册成功：id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved, rolesOf(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        MemberEntity entity = requireMember(id);
        return toDomain(entity, rolesOf(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberResponseById(Long id) {
        MemberEntity entity = requireMember(id);
        return toResponse(entity, rolesOf(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers() {
        return memberRepository.findAll().stream()
                .map(entity -> toResponse(entity, rolesOf(entity)))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberResponse changeStatus(Long id, String status) {
        log.info("变更成员状态：id={}, targetStatus={}", id, status);
        MemberStatus target = parseStatus(status);
        MemberEntity entity = requireMember(id);
        entity.setStatus(target);
        MemberEntity saved = memberRepository.save(entity);
        log.info("状态变更完成：id={}, status={}", id, target);
        return toResponse(saved, rolesOf(saved));
    }

    // ===== 内部工具 =====

    private MemberEntity requireMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "成员不存在: " + id));
    }

    /**
     * 查询成员在 member_role 表中的全部角色，转换为领域角色集合。
     */
    private Set<MemberRole> rolesOf(MemberEntity entity) {
        return memberRoleRepository.findByMemberId(entity.getId()).stream()
                .map(MemberRoleEntity::getRoleType)
                .map(MemberServiceImpl::toMemberRole)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    /**
     * RoleType → MemberRole 映射；无对应领域枚举值的角色忽略。
     */
    private static Optional<MemberRole> toMemberRole(RoleType roleType) {
        return switch (roleType) {
            case WORKER -> Optional.of(MemberRole.WORKER);
            case MERCHANT -> Optional.of(MemberRole.MERCHANT);
            default -> Optional.empty();
        };
    }

    private static MemberStatus parseStatus(String status) {
        try {
            return MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法状态: " + status);
        }
    }

    private static Member toDomain(MemberEntity entity, Set<MemberRole> roles) {
        return new Member(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                roles,
                entity.getRegisteredAt(),
                entity.getStatus(),
                entity.getLaborShares()
        );
    }

    private static MemberResponse toResponse(MemberEntity entity, Set<MemberRole> roles) {
        return new MemberResponse(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                roles,
                entity.getRegisteredAt(),
                entity.getStatus(),
                entity.getLaborShares()
        );
    }
}
