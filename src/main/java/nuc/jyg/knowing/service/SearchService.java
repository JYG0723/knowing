package nuc.jyg.knowing.service;

import nuc.jyg.knowing.model.Question;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ji YongGuang.
 * @date  2017/11/28.
 */
@Service
public class SearchService {

    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda";
    private HttpSolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();
    private static final String QUESTION_TITLE_FIELD = "question_title";
    private static final String QUESTION_CONTENT_FIELD = "question_content";

    public List<Question> searchQuestion(String keyword, int offset, int count,
                                         String hlPre, String hlPos) throws Exception {
        // hlPre前缀   hlPos后缀
        List<Question> questionList = new ArrayList<>();
        //配置搜索条件
        SolrQuery query = new SolrQuery(keyword);//设置solr端本次搜索的关键字
        query.setRows(count);//数量
        query.setStart(offset);//第几条开始
        query.setHighlight(true);//结果高亮
        query.setHighlightSimplePre(hlPre);//前缀
        query.setHighlightSimplePost(hlPos);//后缀
        query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);//"hl.fl"——
        QueryResponse response = client.query(query);//客户端根据该条件查询

        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet())
        {//getHighlighting——Map(Map，List)
            Question q = new Question();
            q.setId(Integer.parseInt(entry.getKey()));//将查询出来的内容填充到空的问题实体中。
            if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                if (contentList.size() > 0) {
                    q.setContent(contentList.get(0));
                }
            }
            if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                if (titleList.size() > 0) {
                    q.setTitle(titleList.get(0));
                }
            }
            questionList.add(q);
        }
        return questionList;//返回查询到的所有内容的集合
    }

    public boolean indexQuestion(int qid, String title, String content) throws Exception {//索引
        SolrInputDocument doc = new SolrInputDocument();//搜的每一个东西都叫Document。类似java中的类和属性
        doc.setField("id", qid);
        doc.setField(QUESTION_TITLE_FIELD, title);
        doc.setField("QUESTION_CONTENT_FIELD", content);
        UpdateResponse response = client.add(doc, 1000);//一千毫秒返回
        return response != null && response.getStatus() == 0;
    }

}
