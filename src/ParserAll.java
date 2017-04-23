import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fk on 2017/04/22.
 */
public class ParserAll {

    private String url = null;
    private Document document = null;
    private PointDensity pointDensity = null;
    private AuthorParser authorParser = null;
    private TimeParser timeParser = null;

    public ParserAll(String url) throws CannotOpenPageException
    {
        this.url = url;
        try {
            Document temp = Jsoup.connect(url).get();
            document = Util.cleanPage(temp);
        } catch (IOException e) {
            throw new CannotOpenPageException(url);
        }
        pointDensity = new PointDensity(document);
        authorParser = new AuthorParser(document);
        timeParser = new TimeParser(document);
    }

    public List<Item> parse()
    {
        List<String> content = pointDensity.parse();
        List<String> author = authorParser.parse();
        List<String> time = timeParser.parser();
        List<Item> res = new ArrayList<>();
        if(time==null || content==null || author==null || content.size()==0 || author.size()==0 || time.size()==0 ) return res;
        res.add(new Item(author.get(0),time.get(0),Util.getTitle(document),content.get(0)));
        int size_author = author.size();
        int size_time = time.size();
        int size_content = content.size();
        int size = Math.max(size_author,Math.max(size_time,size_content));
        for(int i=1;i<size;++i)
        {
            String str_author = (i>=size_author)?"":author.get(i);
            String str_time = (i>=size_time)?"":time.get(i);
            String str_content = (i>=size_content)?"":content.get(i);
            res.add(new Item(str_author,str_time,null,str_content));
        }
        return res;
    }
}
