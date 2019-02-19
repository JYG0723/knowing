package nuc.jyg.knowing.async.handler;

import com.alibaba.fastjson.JSONObject;
import nuc.jyg.knowing.async.EventHandler;
import nuc.jyg.knowing.async.EventModel;
import nuc.jyg.knowing.async.EventType;
import nuc.jyg.knowing.model.EntityType;
import nuc.jyg.knowing.model.Feed;
import nuc.jyg.knowing.model.Question;
import nuc.jyg.knowing.model.User;
import nuc.jyg.knowing.service.FeedService;
import nuc.jyg.knowing.service.FollowService;
import nuc.jyg.knowing.service.QuestionService;
import nuc.jyg.knowing.service.UserService;
import nuc.jyg.knowing.util.JedisAdapter;
import nuc.jyg.knowing.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Ji YongGuang.
 * @date 2017/11/9.
 */
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    FeedService feedService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;


    /**
     * 构建新鲜事的数据
     */
    private String buildFeedData(EventModel model) {
        Map<String, String> map = new HashMap<>();

        // 获取触发用户的数据
        User actor = userService.getUser(model.getActorId());
        if (actor == null) {
            return null;
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());

        if (model.getType() == EventType.COMMENT ||
                (model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION)) {

            // 如果是评论或者关注问题
            Question question = questionService.getById(model.getEntityId());
            if (question == null) {
                return null;
            }
            // 获取被关注的问题的一些信息
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        // 为了测试，把model的actorId随机一下
        Random r = new Random();
        model.setActorId(1 + r.nextInt(10));

        // 评论及关注动作 就会构造一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedData(model));// 新鲜事的核心数据(具体化)
        if (feed.getData() == null) {
            // 不支持的feed
            return;
        }
        feedService.addFeed(feed);

        // 获得新鲜事触发者的所有粉丝
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(),
                Integer.MAX_VALUE);
        // 系统队列
        followers.add(0);
        // 给所有粉丝推事件
        for (int follower : followers) {
            // 给每个粉丝的timeline队列中推送事件
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW});
    }
}
