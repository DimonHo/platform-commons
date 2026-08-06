package com.platformcommons.identity.application;

import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.domain.member.Member;

import java.util.List;

/**
 * 成员服务接口。
 *
 * <p>阿里规范要求 Service 层面向接口编程，由 {@code impl} 包下实现类提供具体逻辑。</p>
 */
public interface MemberService {

    /**
     * 注册新成员。
     *
     * @param request 注册请求
     * @return 注册后的成员视图
     */
    MemberResponse register(MemberRegisterRequest request);

    /**
     * 根据成员 ID 查询成员。
     *
     * @param id 成员 ID
     * @return 成员领域模型
     */
    Member getMemberById(Long id);

    /**
     * 根据成员 ID 查询成员视图。
     *
     * @param id 成员 ID
     * @return 成员响应 DTO
     */
    MemberResponse getMemberResponseById(Long id);

    /**
     * 查询全部成员（分页简化版）。
     *
     * @return 成员响应列表
     */
    List<MemberResponse> listMembers();

    /**
     * 变更成员状态（激活/暂停/退出）。
     *
     * @param id     成员 ID
     * @param status 目标状态
     * @return 变更后的成员视图
     */
    MemberResponse changeStatus(Long id, String status);
}
