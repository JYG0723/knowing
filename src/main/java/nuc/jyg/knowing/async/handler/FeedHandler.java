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
 * @date   2017/11/9.
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


    private String buildFeedData(EventModel model) {
        Map<String, String> map = new HashMap<String ,String>();
        // 触发用户是通用的
        User actor = userService.getUser(model.getActorId());
        if (actor == null) {
            return null;
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());

        if (model.getType() == EventType.COMMENT ||
                (model.getType() == EventType.FOLLOW  && model.getEntityType() == EntityType.ENTITY_QUESTION)) {
            // 如果是评论或者关注问题
            Question question = questionService.getById(model.getEntityId());
            if (question == null) {
                return null;
            }
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        // 为了测试，把model的userId随机一下
        Random r = new Random();
        model.setActorId(1+r.nextInt(10));

        // 构造一个新鲜事
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setUserId(model.getActorId());
        feed.setData(buildFeedData(model));
        if (feed.getData() == null) {
            // 不支持的feed
            return;
        }
        feedService.addFeed(feed);

        // 获得所有粉丝
        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);
        // 系统队列
        followers.add(0);
        // 给所有粉丝推事件
        for (int follower : followers) {
            // 给用户的timeline队列中推送事件
            String timelineKey = RedisKeyUtil.getTimelineKey(follower);
            jedisAdapter.lpush(timelineKey, String.valueOf(feed.getId()));
            // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        // 事件监听器 如果发生这种类型的时间，那么就会触发该监听器的doHandler操作，添加到feedService中，
        // 并且把这些时间推送到所有关注改事件的粉丝的事件队列中。然后用户展示自己timeline的时候就会去自己的事件队列中去取
        return Arrays.asList(new EventType[]{EventType.COMMENT, EventType.FOLLOW});
    }
}
