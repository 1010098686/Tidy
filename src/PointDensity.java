import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by Fk on 2017/04/15.
 */
public class PointDensity {

    private static final char[] set = {'，','。','？','！','；','：','“','”'};

    private Document document = null;
    private HashMap<Element,Double> pointDensity = null;
    private HashMap<Element,Double> pointDensitySum = null;

    public PointDensity(Document document)
    {
        this.document = document;
        pointDensity = new HashMap<Element,Double>();
        pointDensitySum = new HashMap<Element,Double>();
    }

    public List<String> parse()
    {
        List<String> res = new ArrayList<String>();
        Element e = getMaxElement();
        Element ee = findClass(e);
        String className = ee.attr("class");
        res = getContent(className);
        return res;
    }



    private List<String> getContent(String className)
    {
        List<String> res = new ArrayList<String>();
        if(className.isEmpty()) return res;
        Elements elements = document.body().select("[class="+className+"]");
        int size = elements.size();
        for(int i=0;i<size;++i)
        {
            Element e = elements.get(i);
            res.add(e.text());
        }
        return res;
    }

    private Element findClass(Element e)
    {
        while((e.attr("class").equals("") || e.tagName().equals("p"))&&e.parent()!=null) e = e.parent();
        return e;
    }

    private Element getMaxElement()
    {
        processTree();
        double max = 0;
        Map.Entry<Element,Double> maxEntry = null;
        Set<Map.Entry<Element,Double>> entrySet = pointDensitySum.entrySet();
        for(Map.Entry<Element,Double> e:entrySet)
        {
            if(e.getValue() > max)
            {
                max = e.getValue();
                maxEntry = e;
            }
        }
        if(maxEntry==null) maxEntry = entrySet.iterator().next();
        return maxEntry.getKey();
    }

    private void processTree()
    {
        processTree_recursor1(document.body());
        processTree_recursor2(document.body());
    }

    private void processTree_recursor1(Element e)
    {
        Elements elements = e.children();
        String text = e.ownText();
        int pointNum = getPointNum(text);
        int childSize = elements.size();
        int childNum = childSize==0?1:childSize;
        pointDensity.put(e,(double)pointNum/childNum);
        if(childSize==0) return;
        else
        {
            for(int i=0;i<childSize;++i)
            {
                processTree_recursor1(elements.get(i));
            }
        }
    }

    private void processTree_recursor2(Element e)
    {
        double density = pointDensity.get(e);
        Elements elements = e.children();
        int childSize = elements.size();
        if(childSize==0)
        {
            pointDensitySum.put(e,density);
            return;
        }
        else
        {
            double sum = 0;
            for(int i=0;i<childSize;++i)
            {
                sum+=pointDensity.get(elements.get(i));
            }
            pointDensitySum.put(e,sum);
            for(int i=0;i<childSize;++i)
            {
                processTree_recursor2(elements.get(i));
            }
        }
    }

    private int getPointNum(String text)
    {
        int size = text.length();
        int sum = 0;
        for(int i=0;i<size;++i)
        {
            char ch = text.charAt(i);
            if(isPoint(ch)) ++sum;
        }
        return sum;
    }

    private boolean isPoint(char ch)
    {
        int size = set.length;
        for(int i=0;i<size;++i)
        {
            if(ch == set[i]) return true;
        }
        return false;
    }
}
