package nuc.jyg.knowing.controller;

import nuc.jyg.knowing.model.EntityType;
import nuc.jyg.knowing.model.Feed;
import nuc.jyg.knowing.model.HostHolder;
import nuc.jyg.knowing.service.FeedService;
import nuc.jyg.knowing.service.FollowService;
import nuc.jyg.knowing.util.JedisAdapter;
import nuc.jyg.knowing.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ji YongGuang.
 * @date 2017/11/15.
 * Timeline
 * <p>
 * pull 和 push 最大的区别是对于 push 操作，feed 还被额外分发到了 用户 的事件队列(reids)中。
 * 也就促使FeedController中对于 push操作来说是从redis中去取，而对于pull操作是去mysql中取。
 * </p>
 */
@Controller
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 使用于活跃用户
     * 优势:实时性高 缺点:占用存储空间大
     */
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model) {

        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;

        // 取该用户 feed 队列中的最新的10条数据(所关注的人的最新10条事件)
        List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 10);
        List<Feed> feeds = new ArrayList<Feed>();
        for (String feedId : feedIds) {
            Feed feed = feedService.getById(Integer.parseInt(feedId));
            if (feed != null) {
                feeds.add(feed);
            }
        }

        model.addAttribute("feeds", feeds);
        return "feeds";
    }

    /**
     * 适用于僵尸号
     * 登录的时候从新鲜事集合中 获取一次即可，而不用像推的方式每一条信息都会发很多份。
     * 优势: 占用的存储空间小
     * 缺点: 实时性低
     */
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPullFeeds(Model model) {

        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;

        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 获取我全部关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }

        // 从我关注的人中默认一次拉10条feed。排列时间由最近到最远
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);
        model.addAttribute("feeds", feeds);
        return "feeds";
    }
}
