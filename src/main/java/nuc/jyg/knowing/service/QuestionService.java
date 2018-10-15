package nuc.jyg.knowing.service;

import nuc.jyg.knowing.dao.QuestionDAO;
import nuc.jyg.knowing.model.Question;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ji YongGuang.
 * @date  2017/11/15.
 */
@Service
public class QuestionService implements InitializingBean {
    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    SensitiveService sensitiveService;

    public Question getById(int id) {
        return questionDAO.getById(id);
    }

    private int start;
    private int end;

    private ArrayList<String> arrayList = new ArrayList<>(1000);

    public int addQuestion(Question question) {
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        // 敏感词过滤
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));
        return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
    }

    public int search(String p, String t) {
        int N = t.length();
        int M = p.length();
        int i = 0; // 主串的索引
        int j = 0; // 字串的索引

        // 若没到任一字符串的末尾，循环之
        while (i < N && j < M) {
            // 字符相同时，索引都加1
            if (p.charAt(j) == t.charAt(i)) {
                i++;
                j++;
            } else {
                i = i - j + 1; // 这句是关键
                j = 0;
            }
        }
        // 跳出循环的时候不是i == N（没找到）就是j == M（找到）
        if (j == M) {
            start = j;
            end = i;
            return i - j;
        } else {
            return -1;
        }
    }


    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("CensorWords.txt");
        InputStreamReader read = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt;
        while ((lineTxt = bufferedReader.readLine()) != null) {
            lineTxt = lineTxt.trim();
            arrayList.add(lineTxt);
        }
    }
}
