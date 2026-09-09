package cc.wutao.controller.blog;

import cc.wutao.result.Result;
import cc.wutao.service.profile.FriendLinkService;
import cc.wutao.vo.FriendLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 博客端友链接口
 */
@RestController("blogFriendLinkController")
@RequestMapping("/blog/friendLink")
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    /**
     * 获取可见友情链接
     */
    @GetMapping
    public Result<List<FriendLinkVO>> getVisibleFriendLink() {
        List<FriendLinkVO> friendLinkVOList = friendLinkService.getVisibleFriendLink();
        return Result.success(friendLinkVOList);
    }
}
