package com.platformcommons.identity.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.enums.MemberStatus;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.domain.member.MemberEntity;
import com.platformcommons.identity.domain.member.MemberRepository;
import com.platformcommons.identity.domain.role.MemberRole;
import com.platformcommons.identity.domain.role.MemberRoleEntity;
import com.platformcommons.identity.domain.role.MemberRoleRepository;
import com.platformcommons.identity.domain.role.RoleStatus;
import com.platformcommons.identity.domain.role.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link MemberServiceImpl} 单元测试（纯 Mockito，不依赖 DB / Spring 上下文）。
 *
 * <p>覆盖：注册成功、手机号重复、非法状态变更；角色数据源为 member_role 表。</p>
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberRoleRepository memberRoleRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    void register_success_savesOnceAndReturnsMaskedView() {
        // 手机号唯一
        when(memberRepository.findByPhone("13800138000")).thenReturn(Optional.empty());
        // 注册后 member_role 表无角色记录
        when(memberRoleRepository.findByMemberId(1L)).thenReturn(List.of());

        MemberEntity saved = savedMember(1L);
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(saved);

        MemberResponse response = memberService.register(new MemberRegisterRequest("张三", "13800138000"));

        assertEquals("138****8000", response.phone(), "手机号应脱敏");
        assertNull(response.laborShares(), "未设置劳动份额时 laborShares 应为 null");
        assertTrue(response.roles().isEmpty(), "无角色记录时 roles 应为空集");
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
    }

    @Test
    void register_rolesComeFromMemberRoleTable() {
        when(memberRepository.findByPhone("13900139000")).thenReturn(Optional.empty());
        when(memberRoleRepository.findByMemberId(2L)).thenReturn(
                List.of(roleEntity(2L, RoleType.WORKER), roleEntity(2L, RoleType.ADMIN)));

        MemberEntity saved = savedMember(2L);
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(saved);

        MemberResponse response = memberService.register(new MemberRegisterRequest("李四", "13900139000"));

        // 仅映射存在对应 MemberRole 的角色，ADMIN 无对应枚举值应被忽略
        assertEquals(Set.of(MemberRole.WORKER), response.roles());
        verify(memberRoleRepository).findByMemberId(2L);
    }

    @Test
    void register_duplicatePhone_throwsDataDuplicated() {
        MemberEntity existed = savedMember(1L);
        when(memberRepository.findByPhone("13800138000")).thenReturn(Optional.of(existed));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.register(new MemberRegisterRequest("张三", "13800138000")));

        assertEquals(ResultCode.DATA_DUPLICATED.getCode(), ex.getCode());
        verify(memberRepository, never()).save(any(MemberEntity.class));
    }

    @Test
    void changeStatus_invalidStatus_throwsParamInvalid() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> memberService.changeStatus(1L, "UNKNOWN"));

        assertEquals(ResultCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    // ===== 测试工具 =====

    private static MemberEntity savedMember(Long id) {
        MemberEntity entity = new MemberEntity();
        entity.setId(id);
        entity.setName("张三");
        entity.setPhone("13800138000");
        entity.setStatus(MemberStatus.ACTIVE);
        entity.setRegisteredAt(LocalDateTime.now());
        return entity;
    }

    private static MemberRoleEntity roleEntity(Long memberId, RoleType roleType) {
        MemberRoleEntity entity = new MemberRoleEntity();
        entity.setMemberId(memberId);
        entity.setRoleType(roleType);
        entity.setStatus(RoleStatus.ACTIVE);
        entity.setAppliedAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
