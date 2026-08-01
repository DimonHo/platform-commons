package com.platformcommons.identity.api;

import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 成员对外接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装为 {@code R<T>}。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 注册成员。
     */
    @PostMapping("/api/members")
    public MemberResponse register(@Valid @RequestBody MemberRegisterRequest request) {
        log.info("收到注册请求：name={}", request.name());
        return memberService.register(request);
    }

    /**
     * 查询单个成员。
     */
    @GetMapping("/api/members/{id}")
    public MemberResponse getMember(@PathVariable Long id) {
        return memberService.getMemberResponseById(id);
    }

    /**
     * 查询全部成员。
     */
    @GetMapping("/api/members")
    public List<MemberResponse> listMembers() {
        return memberService.listMembers();
    }

    /**
     * 变更成员状态。
     */
    @PutMapping("/api/members/{id}/status")
    public MemberResponse changeStatus(@PathVariable Long id,
                                       @RequestParam String status) {
        log.info("收到状态变更请求：id={}, status={}", id, status);
        return memberService.changeStatus(id, status);
    }
}
