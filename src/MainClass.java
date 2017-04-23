import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Fk on 2017/04/12.
 */
public class MainClass {

    public static void main(String[] args) {
        if(args.length==0)
        {
            System.out.println("url:");
            Scanner scanner = new Scanner(System.in);
            String url = scanner.nextLine();
            String result = doParse(url);
            System.out.println(result);
        }
        else if(args.length!=2)
        {
            System.out.println("wrong usage");
        }
        else
        {
            String param = args[0];
            String value = args[1];
            if("-u".equals(param))
            {
                String result = doParse(value);
                System.out.println(result);
            }
            else if("-f".equals(param))
            {
                File f = new File(value);
                if(!f.exists())
                {
                    System.out.println("no such file");
                }
                else
                {
                    try {
                        File dir = new File("./result");
                        if(!dir.exists()) {
                            dir.mkdir();
                        }
                        int fileIndex = 1;
                        BufferedReader reader = new BufferedReader(new FileReader(f));
                        String url = null;
                        while((url=reader.readLine())!=null)
                        {
                            String result = doParse(url);
                            writeFile(result,fileIndex);
                            fileIndex++;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                System.out.println("wrong usage");
            }
        }
    }

    private static void writeFile(String str,int index)
    {
        File f = new File("./result/"+index+".txt");
        if(!f.exists())
        {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter writer = new PrintWriter(f);
            writer.println(str);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String doParse(String url)
    {
        List<Item> list = null;
        HtmlParser htmlParser = null;
        try {
            htmlParser = new HtmlParser(url);
        } catch (CannotOpenPageException e) {
            System.out.println(e.getMessage());
            return "";
        }
        list = htmlParser.parse();
        if (list.size() != 0) {
            String str = formatItem(list,url);
            return str;
        } else {
            ParserAll parserAll = null;
            try {
                parserAll = new ParserAll(url);
            } catch (CannotOpenPageException e) {
                System.out.println(e.getMessage());
                return "";
            }
            list = parserAll.parse();
            if(list.size()==0) return url;
            String str = formatItem(list,url);
            return str;
        }
    }

    private static String formatItem(List<Item> list,String url)
    {
        JsonObject root = new JsonObject();
        Item post = list.get(0);
        root.add("post",post.toJson());
        JsonArray array = new JsonArray();
        for(int i=1;i<list.size();++i)
        {
            Item reply = list.get(i);
            array.add(reply.toJson());
        }
        root.add("replys",array);
        return url+"\t"+root.toString();
    }
}
