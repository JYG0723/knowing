package nuc.jyg.knowing.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ji YongGuang.
 * @date  2017/11/24.
 */
@Service
public class SensitiveService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);

    /**
     * 默认敏感词替换符
     */
    private static final String DEFAULT_REPLACEMENT = "敏感词";


    private class TrieNode {

        /**
         * true 关键词的终结 ； false 继续
         */
        private boolean end = false;

        /**
         * Map中存储的是子字符的集合 key下一个字符，value是对应的节点
         */
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        /**
         * 向指定位置添加节点树
         */
        void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        /**                                                    (鬼,Node)
         * 获取每个节点下的字典树中  key对应的value节点。  色 ->  (情,Node)
         *                                                      (魔,Node)
         */
        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeywordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }

        public int getSubNodeCount() {
            return subNodes.size();
        }


    }


    /**
     * 根节点
     */
    private TrieNode rootNode = new TrieNode();


    /**
     * 判断是否是一个符号
     */
    private boolean isSymbol(char c) {
        int ic = (int) c;
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    /**
     * 过滤 敏感词
     * <p>
     * 1、如果过滤的文本是空文本直接结束，否则直接从移动指针的位置取第一个元素开始与前缀树中的节点进行比较，如果比较字符串 position 节点处的元素在前缀树中仍存在的话，那么前缀树的当前定位节点 tempNode 节点移到指针到该节点，并继续字符串的下一次笔记。
     * 2、对该文本的每个字符进行迭代过滤，如果是非法字符调用isSymbol进行过滤。如果该非法字符并不掺杂在已被判定的敏感字符后(即前缀树的tempNode仍旧指向rootNode)，那么将该非法字符添加到结果集StringBuilder的result对象中。否则即该非法字符掺杂在敏感词中(你好X色**情XX)，比如这里的**，我们需要跳过这次循环，并不将该非法字符添加到结果集中。并继续将比较指针向后移动一位。
     * 3、
     *
     * </p>
     */
    public String filter(String text) {
        // 空格不过滤
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String replacement = DEFAULT_REPLACEMENT;
        StringBuilder result = new StringBuilder();

        TrieNode tempNode = rootNode;
        int begin = 0; // 上指针，定位指针
        int position = 0; // 比较指针，移动指针

        while (position < text.length()) {
            char c = text.charAt(position);
            // 空格直接跳过
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }

            tempNode = tempNode.getSubNode(c);

            // 当前位置的匹配结束
            if (tempNode == null) {
                // 以begin开始的字符串不存在敏感词
                result.append(text.charAt(begin));
                // 跳到下一个字符开始测试
                position = begin + 1;
                begin = position;
                // 回到树初始节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词， 从begin到position的字符位置 用replacement替换掉
                result.append(replacement);
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            } else {
                ++position;
            }
        }

        result.append(text.substring(begin));

        return result.toString();
    }

    /**
     * 构件树
     * <p>
     * 构件树的过程。
     * 1、把添加进来的词汇过滤掉非法词汇(构建了一个私有函数用来判断当前截取的字符是否是非法字符，如果是的话提过本次循环)
     * 2、如果不是非法字符，判断前缀树中当前是否有该字符，如果已经有了的话。让tempNode(标记节点) = 该节点。
     * 3、继续循环向下遍历这个敏感词看是否前缀树已经存在，如果不存在那么构建这个节点，并且将该节点添加到当前节点 tempNode 的子节点。
     * 4、最后如果确定了一个敏感词，遍历完了该敏感词，那么应该在循环的末尾把该条树的路径的最后一个节点标记上敏感词标记。
     * </p>
     *
     * @param lineTxt
     */
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        // 循环每个字符
        for (int i = 0; i < lineTxt.length(); ++i) {
            Character c = lineTxt.charAt(i);
            // 过滤空格,非法字符
            if (isSymbol(c)) {
                continue;
            }
            TrieNode node = tempNode.getSubNode(c);

            if (node == null) { // 该词汇没初始化
                node = new TrieNode();
                tempNode.addSubNode(c, node);
            }

            tempNode = node;

            if (i == lineTxt.length() - 1) {
                // 关键词结束， 设置结束标志
                tempNode.setKeywordEnd(true);
            }
        }
//        System.out.println(UUID.randomUUID().toString());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        rootNode = new TrieNode();

        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                addWord(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

    public static void main(String[] argv) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("色鬼");
        s.addWord("好色");
        System.out.print(s.filter("你好X色**情XX"));
    }
}
