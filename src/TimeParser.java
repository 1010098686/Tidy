import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Created by Fk on 2017/04/22.
 */
public class TimeParser {

    private Document document = null;

    public TimeParser(Document document) {
        this.document = document;
    }

    public List<String> parser() {
        String plainText = Util.cleanLabels(document);
        List<String> times = Util.getTime(plainText);
        HashMap<String, List<String>> map = new HashMap<>();
        for (String time : times) {
            String className = findClass(time);
            if (map.containsKey(className)) {
                map.get(className).add(time);
            } else {
                List<String> list = new ArrayList<>();
                list.add(time);
                map.put(className, list);
            }
        }
        List<String> max = null;
        Set<Map.Entry<String,List<String>>> entrySet = map.entrySet();
        for(Map.Entry<String,List<String>> entry:entrySet)
        {
            if(max==null) max=entry.getValue();
            else if(entry.getValue().size() > max.size()) max = entry.getValue();
        }
        return max;
    }

    private String findClass(String time) {
        Element element = find_recursor(document.body(), time);
        if (element == null) return null;
        while (element.attr("class").equals("")) element = element.parent();
        return element.attr("class");
    }

    private Element find_recursor(Element element, String time) {
        if (element.hasText()) {
            String text = element.ownText();
            if (text.contains(time)) return element;
        }

        Elements elements = element.children();
        if (elements.size() == 0) return null;
        else {
            int size = elements.size();
            for (int i = 0; i < size; ++i) {
                Element res = find_recursor(elements.get(i), time);
                if (res != null) return res;
            }
            return null;
        }
    }
}
