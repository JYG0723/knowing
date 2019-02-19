package nuc.jyg.knowing.service;

import nuc.jyg.knowing.dao.FeedDAO;
import nuc.jyg.knowing.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ji YongGuang.
 * @date 2017/11/12.
 */
@Service
public class FeedService {
    @Autowired
    FeedDAO feedDAO;

    /**
     * 取我关注的人的最新count条数据
     *
     * @param maxId   获取的feed最小的id，递增式的获取
     * @param userIds 我关注的人的id集合
     * @param count   元素个数
     */
    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, int count) {
        return feedDAO.selectUserFeeds(maxId, userIds, count);
    }

    public boolean addFeed(Feed feed) {
        feedDAO.addFeed(feed);
        return feed.getId() > 0;
    }

    public Feed getById(int id) {
        return feedDAO.getFeedById(id);
    }
}
