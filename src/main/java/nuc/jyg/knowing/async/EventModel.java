package nuc.jyg.knowing.async;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ji YongGuang.
 * @date   2017/11/9.
 */
public class EventModel {

    /**
     * 事件类型
     */
    private EventType type;
    /**
     * 触发者
     */
    private int actorId;
    /**
     * 实体类型
     */
    private int entityType;
    /**
     * 实体id
     */
    private int entityId;
    /**
     * 实体所有者
     */
    private int entityOwnerId;

    /**
     * 事件額外信息
     */
    private Map<String, String> exts = new HashMap<String, String>();

    public EventModel() {

    }

    public EventModel setExt(String key, String value) {
        exts.put(key, value);
        return this;
    }

    public EventModel(EventType type) {
        this.type = type;
    }

    public String getExt(String key) {
        return exts.get(key);
    }


    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel setExts(Map<String, String> exts) {
        this.exts = exts;
        return this;
    }
}
