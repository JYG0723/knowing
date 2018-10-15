package nuc.jyg.knowing.controller;

import nuc.jyg.knowing.model.EntityType;
import nuc.jyg.knowing.model.Question;
import nuc.jyg.knowing.model.ViewObject;
import nuc.jyg.knowing.service.FollowService;
import nuc.jyg.knowing.service.QuestionService;
import nuc.jyg.knowing.service.SearchService;
import nuc.jyg.knowing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ji YongGuang.
 * @date   2017/11/24.
 */
@Controller
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    SearchService searchService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @RequestMapping(path = {"/search"}, method = {RequestMethod.GET})
    public String search(Model model, @RequestParam("q") String keyword,
                         @RequestParam(value = "offset", defaultValue = "0") int offset,
                         @RequestParam(value = "count", defaultValue = "10") int count) {
        try {
            List<Question> questionList = searchService.searchQuestion(keyword, offset, count,//搜索的模板的集合。solr服务器端查询的结果
                    "<em>", "</em>");
            List<ViewObject> vos = new ArrayList<>();
            for (Question question : questionList) {
                Question q = questionService.getById(question.getId());//根据solr端查询到的内容，找到数据库中的对应的问题
                ViewObject vo = new ViewObject();
                if (question.getContent() != null) {//用从solr服务器端查询出来的问题和内容替换从数据库中找到的问题中的对应处。因为服务器端查询出来的内容中的关键字高亮美化处理过
                    q.setContent(question.getContent());
                }
                if (question.getTitle() != null) {
                    q.setTitle(question.getTitle());
                }
                vo.set("question", q);
                vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                vo.set("user", userService.getUser(q.getUserId()));
                vos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("keyword", keyword);
        } catch (Exception e) {
            logger.error("搜索评论失败" + e.getMessage());
        }
        return "result";
    }
}
