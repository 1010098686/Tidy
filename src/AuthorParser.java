import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Fk on 2017/04/18.
 */
public class AuthorParser {

    private static final int THRESHOLD = 3;
    private static final int FLOAT_THRESHOLD = 0;

    private Document document = null;

    public AuthorParser(Document document)
    {
        this.document = document;
    }

    public List<String> parse()
    {
        HashMap<String,Integer> map = generateHashMap();
        map = removeUselessNode(map);
        Set<String> set = statisticAnchor(map);
        Set<String> classNames = getClassNames(map,set);
        classNames = filterClassNames(classNames);
        //List<String> contents = getContents(classNames);
        List<String> res = getAuthor(classNames);
        return res;

    }

    private List<String> getAuthor(Set<String> set)
    {
        String maxClassName = null;
        double maxRate = 0;
        int maxIndex = 0;
        Iterator<String> ite = set.iterator();
        while(ite.hasNext())
        {
            String className = ite.next();
            Elements elements = document.body().select("div[class="+className+"]");
            int size = elements.size();
            int index = 0;
            if(size>=2)
            {
                Element element1 = elements.get(0);
                Element element2 = elements.get(1);
                if(!element1.text().equals("") && !element2.text().equals("")) {
                    String[] tokens1 = element1.text().split(" ");
                    String[] tokens2 = element2.text().split(" ");
                    try {
                        while (tokens1[index].equals(tokens2[index])) {
                            ++index;
                        }
                    }catch(ArrayIndexOutOfBoundsException e)
                    {
                        index=0;
                    }
                }
            }
            int sum = 0;
            for(int i=0;i<size;++i)
            {
                String text = elements.get(i).text();
                String token = null;
                try {
                    token = text.split(" ")[index];
                }catch(ArrayIndexOutOfBoundsException e)
                {
                    token = "";
                }
                if(token.length()<=15 && token.length()>=3) ++sum;
            }
            double rate = (double)sum/size;
            if(rate > maxRate)
            {
                maxRate = rate;
                maxClassName = className;
                maxIndex = index;
            }
        }
        List<String> res = new ArrayList<>();
        Elements elements = document.body().select("div[class="+maxClassName+"]");
        int size = elements.size();
        for(int i=0;i<size;++i)
        {
            Element element = elements.get(i);
            String userName = element.text().split(" ")[maxIndex];
            res.add(userName);
        }
        return res;
    }

    private boolean needToDelete(Element element)
    {
        Elements elements = element.children();
        if(elements.size()==0 && !element.hasText()) return true;
        return false;
    }

    private Set<String> filterClassNames(Set<String> set)
    {
        Set<String> res = new HashSet<>();
        res.addAll(set);
        Iterator<String> ite = res.iterator();
        while(ite.hasNext())
        {
            String className = ite.next();
            Elements elements = document.body().select("div[class="+className+"]");
            int size = elements.size();
            //situation1 是叶子节点并且没有文本信息的class要被去除
            if(size==1)
            {
                if(needToDelete(elements.get(0)))
                {
                    ite.remove();
                    continue;
                }
            }
            else if(size==2)
            {
                if(needToDelete(elements.get(0)) && needToDelete(elements.get(1)))
                {
                    ite.remove();
                    continue;
                }
            }
            else
            {
                Element element1 = elements.get(1);
                Element element2 = elements.get(2);
                if(needToDelete(element1) && needToDelete(element2))
                {
                    ite.remove();
                    continue;
                }
            }
            //situation2 文本信息中重复文字的数量大于二的要被去除
            if(size>=3)
            {
                Element element1 = elements.get(1);
                Element element2 = elements.get(2);
                String text1 = element1.text();
                String text2 = element2.text();
                String[] tokens1 = text1.split("(,| )");
                String[] tokens2 = text2.split("(,| )");
                int i=0,size1=tokens1.length,size2=tokens2.length;
                int sum = 0;
                while(i<size1 && i<size2)
                {
                    String token1 = tokens1[i];
                    String token2 = tokens2[i];
                    ++i;
                    if(token1.equals(token2)) ++sum;
                }
                if(sum>=2)
                {
                    ite.remove();
                    continue;
                }
            }
            //situation3 节点中所有文字的长度都是一样的，表明该节点可能是噪声信息，要被去除
            boolean flag = true;
            for(int i=2;i<size;++i)//跳过楼主信息
            {
                String text1 = elements.get(i).text();
                String text2 = elements.get(i-1).text();
                if(text1.length()!=text2.length())
                {
                    flag=false;
                }
            }
            if(flag)
            {
                ite.remove();
            }
        }
        return res;
    }

    private List<String> getContents(Set<String> classNames)
    {
        Iterator<String> ite = classNames.iterator();
        List<String> contents = new ArrayList<String>();
        while(ite.hasNext())
        {
            String className = ite.next();
            Elements elements = document.body().select("div[class="+className+"]");
            int size = elements.size();
            for(int i=0;i<size;++i)
            {
                Element element = elements.get(i);
                String text = element.text();
                if(!text.equals("")) contents.add(text);
            }
        }
        return contents;
    }

    private Set<String> getClassNames(HashMap<String,Integer> map,Set<String> set)
    {
        int size = set.size();
        Set<String> classNames = new HashSet<String>();
        Set<Map.Entry<String,Integer>> entrySet = map.entrySet();
        if(size == 1)
        {
            String key = set.iterator().next();
            for(Map.Entry<String,Integer> entry:entrySet)
            {
                if(Math.abs(entry.getValue()-map.get(key))<=FLOAT_THRESHOLD)
                {
                    classNames.add(entry.getKey());
                }
            }
        }
        else
        {
            Iterator<String> ite = set.iterator();
            int maxNum = 0;
            int number = 0;
            while(ite.hasNext())
            {
                String key = ite.next();
                int num = map.get(key);
                int appearNum = valueNum(map,num);
                if(appearNum>maxNum)
                {
                    maxNum = appearNum;
                    number = num;
                }
            }
//            for(Map.Entry<String,Integer> entry:entrySet)
//            {
//                if(Math.abs(entry.getValue()-number)<=FLOAT_THRESHOLD || entry.getValue()%number ==0)
//                {
//                    classNames.add(entry.getKey());
//                }
//            }
            for(String str:set)
            {
                if(Math.abs(map.get(str)-number)<=FLOAT_THRESHOLD || map.get(str)%number ==0)
                {
                    classNames.add(str);
                }
            }
        }
        return classNames;
    }

    private int valueNum(HashMap<String,Integer> map,int num)
    {
        int sum = 0;
        Set<Map.Entry<String,Integer>> entrySet = map.entrySet();
        for(Map.Entry<String,Integer> entry:entrySet)
        {
            if(entry.getValue() == num) ++sum;
        }
        return sum;
    }

    private Set<String> statisticAnchor(HashMap<String,Integer> map)
    {
        Set<String> keys = map.keySet();
        Iterator<String> ite = keys.iterator();
        Set<String> res = new HashSet<String>();
        while(ite.hasNext())
        {
            String key = ite.next();
            Elements elements = document.body().select("div[class="+key+"]");
            int size = elements.size();
            int index = (int)(Math.random()*size);
            //int index = 0;
            Element element = elements.get(index);
            int rate = calculateAnchorRate(element);
            if(rate!=0)
            {
                res.add(key);
            }
        }
        return res;
    }

    private int calculateAnchorRate(Element element)
    {
        //Elements all = element.getAllElements();
        Elements a = element.select("a");
        int num1 = a.size();
        //double num2 = all.size();
        return num1;
//        return 0;
    }

    private HashMap<String,Integer> removeUselessNode(HashMap<String,Integer> map)
    {
        Set<String> keys = map.keySet();
        Iterator<String> ite = keys.iterator();
        while(ite.hasNext())
        {
            String key = ite.next();
            int value = map.get(key);
            if(value<THRESHOLD)
            {
                ite.remove();
            }
        }
        return map;
    }
    private HashMap<String,Integer> generateHashMap()
    {
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        Elements elements = document.body().select("div");
        int size = elements.size();
        for(int i=0;i<size;++i)
        {
            Element element = elements.get(i);
            String className = element.attr("class");
            if(!className.equals(""))
            {
                boolean flag = map.containsKey(className);
                if(flag)
                {
                    int value = map.get(className);
                    map.replace(className,value+1);
                }
                else
                {
                    map.put(className,1);
                }
            }
        }
        return map;
    }


}
