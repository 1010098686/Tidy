import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Fk on 2017/04/12.
 */
public class Item {

    public String id;
    public String time;
    public String title;
    public String content;

    public Item()
    {
        id = null;
        time = null;
        title = null;
        content = null;
    }

    public Item(String id,String time,String title,String content)
    {
        this.id = id;
        this.time = time;
        this.title = title;
        this.content = content;
    }

    @Override
    public String toString() {
        return "id: "+ id + "\ntime: " + time + "\ntitle: " + title + "\ncontent: " + content + "\n";
    }

    public JsonObject toJson()
    {
        JsonObject object = new JsonObject();
        if(id!=null)
        {
            object.addProperty("author",id);
        }
        if(title!=null)
        {
            object.addProperty("title",title);
        }
        if(content!=null)
        {
            object.addProperty("content",content);
        }
        if(time!=null)
        {
            object.addProperty("publish_date",formatTime());
        }
        return object;
    }

    private String formatTime()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date data = df.parse(time);
            return df.format(data);
        } catch (ParseException e) {
            return this.time;
        }

    }
}
