import com.spire.doc.*;
import com.spire.doc.documents.Paragraph;
import com.spire.doc.documents.TextSelection;
import com.spire.doc.fields.CheckBoxFormField;
import com.spire.doc.fields.DocPicture;
import com.spire.doc.fields.TextRange;
import java.util.HashMap;
import java.util.Map;

public class TestGenDoc {

    public static void main(String[] args) {
        Document document = new Document("H:\\“上海市健康科普优秀作品”推荐表.doc");
        Section section = document.getSections().get(0);
        Table table = section.getTables().get(0);
        Map<String, String> map = new HashMap<>();
        map.put("firstName", "Alex");
        map.put("lastName", "Anderson");
        map.put("test", "（ ）图文类 □科普文章 □漫  画  □海报折页 □其他\n" +
                "（ ）音频类 □专题音频 □广播剧  □有声书   □其他\n" +
                "（ ）视频类 ○单集作品         ○系列作品\n" +
                "□短视频(≤10分钟)  □长视频(＞10分钟) ");
        int i = 0;
        for(TableRow row:(Iterable<TableRow>)table.getRows()){
            for(TableCell cell : (Iterable<TableCell>)row.getCells()){
                for(Paragraph para : (Iterable<Paragraph>)cell.getParagraphs()){
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                            para.replace("${" + entry.getKey() + "}", entry.getValue(), false, true);
                    }
                }
            }
        }
        document.saveToFile("H:/filled1.docx", FileFormat.Docx_2013);
    }
}
