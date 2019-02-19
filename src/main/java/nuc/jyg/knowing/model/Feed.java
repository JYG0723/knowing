package nuc.jyg.knowing.model;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * @author Ji YongGuang.
 * @date 2017/11/12.
 */
public class Feed {

    private int id;
    // 新鲜事的类型 关注，评论等
    private int type;
    // 谁发出的新鲜事
    private int userId;
    // 新鲜事创建时间
    private Date createdDate;
    // 新鲜事的核心数据
    private String data;
    // 核心数据对应的Json串
    private JSONObject dataJSON = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        dataJSON = JSONObject.parseObject(data);
    }

    public String get(String key) {
        return dataJSON == null ? null : dataJSON.getString(key);
    }
}
