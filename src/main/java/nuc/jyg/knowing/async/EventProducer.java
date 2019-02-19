package nuc.jyg.knowing.async;

import com.alibaba.fastjson.JSONObject;
import nuc.jyg.knowing.util.JedisAdapter;
import nuc.jyg.knowing.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ji YongGuang.
 * @date   2017/11/9.
 */
@Service
public class EventProducer {

    @Autowired
    JedisAdapter jedisAdapter;

    public boolean fireEvent(EventModel eventModel) {
        try {
            // 拿到事件队列 并序列化该EventModel 推送到队列中
            String json = JSONObject.toJSONString(eventModel);
            String key = RedisKeyUtil.getEventQueueKey();
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
