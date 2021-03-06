package nuc.jyg.knowing.async;

import com.alibaba.fastjson.JSON;
import nuc.jyg.knowing.util.JedisAdapter;
import nuc.jyg.knowing.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ji YongGuang.
 * @date   2017/11/9.
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
    private ApplicationContext applicationContext;

    @Autowired
    private JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            // <handlerName,EventHandler>
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                // 获取每个EventHandler 负责处理的事件
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                // 迭代这些事件，在这些处理的列表加上这个Handler
                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String key = RedisKeyUtil.getEventQueueKey();

                    // 返回格式 {key,eventModel.json}  每个被返回的eventModel都会带上该list的名字 下面需要对该name进行过滤
                    List<String> events = jedisAdapter.brpop(0, key);

                    for (String message : events) {// 迭代从队列中取出来事件
                        if (message.equals(key)) {
                            continue;
                        }

                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
                        if (!config.containsKey(eventModel.getType())) {// 排除无效事件
                            logger.error("不需要handler处理的事件");
                            continue;
                        }

                        for (EventHandler handler : config.get(eventModel.getType())) {// 处理事件
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
