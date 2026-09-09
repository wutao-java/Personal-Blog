package cc.wutao.controller.admin;

import cc.wutao.annotation.RateLimit;
import cc.wutao.dto.*;
import cc.wutao.result.Result;
import cc.wutao.service.system.AdminService;
import cc.wutao.vo.AdminLoginVO;
import cc.wutao.vo.AdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端管理员接口
 */
@RestController
@RequestMapping("/admin/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 发送验证码
     */
    @PostMapping("/sendCode")
    @RateLimit(type = RateLimit.Type.IP, tokens = 5, burstCapacity = 8,
            timeWindow = 60, message = "操作过于频繁，请稍后再试")
    public Result sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        log.info("发送验证码,{}", sendCodeDTO);
        adminService.sendVerifyCode(sendCodeDTO.getUsername());
        return Result.success();
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @RateLimit(type = RateLimit.Type.IP, tokens = 5, burstCapacity = 8,
            timeWindow = 60, message = "操作过于频繁，请稍后再试")
    public Result<AdminLoginVO> AdminLogin(@Valid @RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录, username={}", adminLoginDTO.getUsername());
        AdminLoginVO adminLoginVO = adminService.login(adminLoginDTO);
        return Result.success(adminLoginVO);
    }

    /**
     * 获取管理员信息
     */
    @GetMapping
    public Result<AdminVO> getAdminInfo() {
        AdminVO adminVO = adminService.getAdminById();
        return Result.success(adminVO);
    }

    /**
     * 管理员退出登录
     */
    @PostMapping("/logout")
    public Result logout(@RequestBody AdminLogoutDTO adminLogoutDTO) {
        log.info("管理员退出登录, id={}", adminLogoutDTO.getId());
        adminService.logout(adminLogoutDTO);
        return Result.success();
    }

    /**
     * 管理员修改密码
     */
    @PutMapping("/changePassword")
    public Result changePassword(@Valid @RequestBody AdminChangePasswordDTO adminChangePasswordDTO) {
        log.info("管理员修改密码");
        adminService.changePassword(adminChangePasswordDTO);
        return Result.success();
    }

    /**
     * 管理员更改昵称
     */
    @PutMapping("/changeNickname")
    public Result changeNickname(@Valid @RequestBody AdminChangeNicknameDTO adminChangeNicknameDTO) {
        log.info("管理员更改昵称：{}", adminChangeNicknameDTO);
        adminService.changeNickname(adminChangeNicknameDTO);
        return Result.success();
    }

    /**
     * 管理员换绑邮箱
     */
    @PutMapping("/changeEmail")
    public Result changeEmail(@Valid @RequestBody AdminChangeEmailDTO adminChangeEmailDTO) {
        log.info("管理员换绑邮箱");
        adminService.changeEmail(adminChangeEmailDTO);
        return Result.success();
    }
}
