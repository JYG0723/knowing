package nuc.jyg.knowing.async.handler;

import nuc.jyg.knowing.async.EventHandler;
import nuc.jyg.knowing.async.EventModel;
import nuc.jyg.knowing.async.EventType;
import nuc.jyg.knowing.model.Message;
import nuc.jyg.knowing.model.User;
import nuc.jyg.knowing.service.MessageService;
import nuc.jyg.knowing.service.UserService;
import nuc.jyg.knowing.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Ji YongGuang.
 * @date   2017/11/9.
 */
@Component
public class LikeHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        message.setContent("用户" + user.getName()
                + "赞了你的评论,http://127.0.0.1:8080/question/" + model.getExt("questionId"));

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}