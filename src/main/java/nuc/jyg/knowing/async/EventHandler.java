package nuc.jyg.knowing.async;

import java.util.List;

/**
 * @author Ji YongGuang.
 * @date   2017/11/9.
 */
public interface EventHandler {
    void doHandle(EventModel model);

    List<EventType> getSupportEventTypes();
}
