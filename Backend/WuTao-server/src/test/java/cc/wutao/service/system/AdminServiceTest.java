package cc.wutao.service.system;

import cc.wutao.dto.AdminLoginDTO;
import cc.wutao.entity.Admin;
import cc.wutao.mapper.AdminMapper;
import cc.wutao.properties.VisitorProperties;
import cc.wutao.service.auth.EncryptPasswordService;
import cc.wutao.service.auth.TokenService;
import cc.wutao.service.auth.VerifyCodeService;
import cc.wutao.service.email.EmailService;
import cc.wutao.vo.AdminLoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    private AdminMapper adminMapper;
    private VerifyCodeService verifyCodeService;
    private TokenService tokenService;
    private EncryptPasswordService encryptPasswordService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminMapper = mock(AdminMapper.class);
        verifyCodeService = mock(VerifyCodeService.class);
        tokenService = mock(TokenService.class);
        encryptPasswordService = mock(EncryptPasswordService.class);
        adminService = new AdminService(
                adminMapper,
                verifyCodeService,
                mock(EmailService.class),
                tokenService,
                mock(VisitorProperties.class),
                encryptPasswordService);
    }

    @Test
    void loginOnlyRequiresUsernameAndPassword() {
        Admin admin = Admin.builder()
                .id(1L)
                .username("admin")
                .password("hashed-password")
                .salt("salt")
                .role(1)
                .build();
        AdminLoginDTO login = AdminLoginDTO.builder()
                .username("admin")
                .password("plain-password")
                .build();

        when(adminMapper.getByUsername("admin")).thenReturn(admin);
        when(encryptPasswordService.hashPassword("plain-password", "salt"))
                .thenReturn("hashed-password");
        when(tokenService.createAndStoreToken(1L, 1)).thenReturn("token");

        AdminLoginVO result = adminService.login(login);

        assertEquals(1L, result.getId());
        assertEquals("token", result.getToken());
        verifyNoInteractions(verifyCodeService);
    }
}
