import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fk on 2017/04/22.
 */
public class Util {

    public static Document cleanPage(Document d)
    {
        String html = d.html();
        String script_pattern1 = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
        String script_pattern2 = "<[\\s]*?noscript[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?noscript[\\s]*?>";
        String script_pattern = "("+script_pattern1+"|"+script_pattern2+")";
        String style_pattern = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
        String comment_pattern = "<!--[\\w\\W\r\\n]*?-->";
        Pattern script = Pattern.compile(script_pattern);
        Pattern style = Pattern.compile(style_pattern);
        Pattern comment = Pattern.compile(comment_pattern);

        Matcher m_script = script.matcher(html);
        html = m_script.replaceAll("");

        Matcher m_style = style.matcher(html);
        html = m_style.replaceAll("");

        Matcher m_comment = comment.matcher(html);
        html = m_comment.replaceAll("");

        Document res = Jsoup.parse(html);
        return res;
    }

    public static List<String> getTime(String str)
    {
        String fmat1 = "((\\d+-)?\\d+-\\d+\\s+\\d+:\\d+)";
        String fmat2 = "((\\d+/)?\\d+/\\d+\\s+\\d+:\\d+)";
        String fmat3 = "((\\d+年)?\\d+月\\d+日\\s+\\d+:\\d+)";
        String fmat4 = "((\\d+\\.)?\\d+\\.\\d+\\s+\\d+:\\d+)";
        String fmat = "("+fmat1+"|"+fmat2+"|"+fmat3+"|"+fmat4+")";
        List<String> res = new ArrayList<>();
        Pattern p = Pattern.compile(fmat);
        Matcher m = p.matcher(str);
        while(m.find())
        {
            res.add(m.group());
        }
        return res;
    }

    public static String cleanLabels(Document document)
    {
        String pattern = "<[^>]+>";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(document.body().html());
        return m.replaceAll("");
    }

    public static String getTitle(Document doc)
    {
        Element head = doc.head();
        Element title = head.select("title").first();
        if(title==null) return "";
        String str = title.text();
        int len = str.length();
        //用_,-,/来切分title标签中的内容，获得有用的标题
        int index1 = str.indexOf("_")>0?str.indexOf("_"):len;
        int index2 = str.indexOf("-")>0?str.indexOf("-"):len;
        int index3 = str.indexOf("/")>0?str.indexOf("/"):len;
        int min = Math.min(index1,Math.min(index2,index3));
        str = str.substring(0,min);
        return str.trim();
    }
}
