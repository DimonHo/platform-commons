package com.platformcommons.identity.api;

import com.platformcommons.common.api.Result;
import com.platformcommons.identity.api.dto.MemberRegisterRequest;
import com.platformcommons.identity.api.dto.MemberResponse;
import com.platformcommons.identity.service.MemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成员对外接口。
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 注册成员。
     */
    @PostMapping
    public ResponseEntity<Result<MemberResponse>> register(@Valid @RequestBody MemberRegisterRequest request) {
        log.info("收到注册请求：name={}", request.name());
        return ResponseEntity.ok(Result.success(memberService.register(request)));
    }

    /**
     * 查询单个成员。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<MemberResponse>> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success(memberService.getMemberResponseById(id)));
    }

    /**
     * 查询全部成员。
     */
    @GetMapping
    public ResponseEntity<Result<List<MemberResponse>>> listMembers() {
        return ResponseEntity.ok(Result.success(memberService.listMembers()));
    }

    /**
     * 变更成员状态。
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Result<MemberResponse>> changeStatus(@PathVariable Long id,
                                                               @RequestParam String status) {
        log.info("收到状态变更请求：id={}, status={}", id, status);
        return ResponseEntity.ok(Result.success(memberService.changeStatus(id, status)));
    }
}
