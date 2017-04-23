import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fk on 2017/04/12.
 */
public class HtmlParser {

    private static final int WINDOW_WIDTH = 100;

    private String url = null;
    private Document document = null;

    public HtmlParser(String str) throws CannotOpenPageException
    {
        this.url = str;
        try {
            Document temp = Jsoup.connect(url).get();
            document = Util.cleanPage(temp);
        } catch (IOException e) {
            throw new CannotOpenPageException(str);
        }
    }

    public List<Item> parse()
    {
        List<Item> res = new ArrayList<>();
        List<String> contents = getContent();
        List<String> times = getTime();
        String title = Util.getTitle(document);
        List<String> authors = getAuthorByLabel();
        if(authors.size()==0) authors = getAuthorByUid();
        if(contents.size()==0 || times.size()==0 || authors.size()==0) return res;
        int size1 = contents.size();
        int size2 = times.size();
        int size3 = authors.size();
        int size = Math.min(size1,Math.min(size2,size3));
        res.add(new Item(authors.get(0),times.get(0),title,contents.get(0)));
        for(int i=1;i<size;++i)
        {
            res.add(new Item(authors.get(i),times.get(i),null,contents.get(i)));
        }
        return res;
    }

    private List<String> getAuthorByLabel()
    {
        List<String> res = new ArrayList<>();
        Elements idNodes = document.body().select("div[class=authi]");
        if(idNodes.size() == 0) return res;
        Elements idClass1 = idNodes.select("a[class=xi2]");
        Elements idClass2 = idNodes.select("a[class=xw1]");
        if(idClass1.size()==0 && idClass2.size()==0) return res;
        if(idClass1.size()!=0)
        {
            int size = idClass1.size();
            for(int i=0;i<size;++i)
            {
                res.add(idClass1.get(i).text());
            }
        }
        else if(idClass2.size()!=0)
        {
            int size = idClass2.size();
            for(int i=0;i<size;++i)
            {
                res.add(idClass2.get(i).text());
            }
        }
        return res;
    }

    private List<String> getAuthorByUid()
    {
        Elements elements = document.body().select("a[href]");
        int size = elements.size();
        List<Element> list = new ArrayList<>();
        for(int i=0;i<size;++i)
        {
            Element element = elements.get(i);
            String href = element.attr("href");
            if(href.contains("uid"))
            {
                list.add(element);
            }
        }
        size = list.size();
        List<String> res = new ArrayList<>();
        for(int i=0;i<size;++i)
        {
            res.add(list.get(i).text());
        }
        return res;
    }

    private List<String> getTime()
    {
        Elements elements = document.body().select("em[id^=authorposton]");
        List<String> res = new ArrayList<>();
        if(elements.size()==0) return res;
        int size = elements.size();
        for(int i=0;i<size;++i)
        {
            List<String> list = Util.getTime(elements.get(i).text());
            if(list.size()==0)
            {
                res.add("unknown time format");
            }
            else
            {
                res.add(list.get(0));
            }
        }
        return res;
    }

    private List<String> getContent()
    {
        Elements elements = document.body().select("td[class=t_f][id^=postmessage]");
        int size = elements.size();
        List<String> res = new ArrayList<>();
        if(size==0) return res;
        for(int i=0;i<size;++i)
        {
            String content = elements.get(i).text();
            res.add(content);
        }
        return res;
    }

    private Item getPost()
    {

        String title = Util.getTitle(document);
        Elements timeNodes = document.body().select("em[id^=authorposton]");
        if(timeNodes.size()==0) return null; //时间标签不存在
        String time = timeNodes.first().text();
        time = Util.getTime(time).get(0);
        Elements idNodes = document.body().select("div[class=authi]");
        if(idNodes.size()==0) return null;  //ID的div标签不存在
        Elements idClass1 = idNodes.select("a[class=xi2]");
        Elements idClass2 = idNodes.select("a[class=xw1]");
        if(idClass1.size()==0 && idClass2.size()==0) return null; //ID的两种a标签都不存在
        String id = null;
        if(idClass1.size()!=0)
        {
            id = idClass1.first().text();
        }
        else
        {
            id = idClass2.first().text();
        }
        Elements contentNodes = document.body().select("td[class=t_f][id^=postmessage]");
        if(contentNodes.size()==0) return null; //正文标签不存在
        String content = contentNodes.first().text();
        return new Item(id,time,title,content);
    }

    private List<Item> getReplys()
    {

        List<Item> res = new ArrayList<Item>();
        Elements timeNodes = document.body().select("em[id^=authorposton]");
        Elements idNodes = document.body().select("div[class=authi]");
        Elements contentNodes = document.body().select("td[class=t_f][id^=postmessage]");
        if(timeNodes.size() == 0) return null;
        if(idNodes.size() == 0) return null;
        if(contentNodes.size() == 0) return null;
        Elements idClass1 = idNodes.select("a[class=xi2]");
        Elements idClass2 = idNodes.select("a[class=xw1]");
        if(idClass1.size() == 0 && idClass2.size() == 0) return null;

        int size = contentNodes.size();
        for(int i=1;i<size;++i)
        {
            String time = Util.getTime(timeNodes.get(i).text()).get(0);

            String id = null;
            if(idClass1.size() != 0)
            {
                id = idClass1.get(i).text();
            }
            else
            {
                id = idClass2.get(i).text();
            }

            String content = contentNodes.get(i).text();

            res.add(new Item(id,time,null,content));
        }
        return res;
    }




}
