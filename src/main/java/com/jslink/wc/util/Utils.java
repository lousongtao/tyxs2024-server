package com.jslink.wc.util;

import com.jslink.wc.pojo.IExperience;
import com.jslink.wc.pojo.IPrize;
import com.jslink.wc.pojo.ISubsidize;
import com.spire.doc.Document;
import com.spire.doc.Table;
import com.spire.doc.TableCell;
import com.spire.doc.TableRow;
import com.spire.doc.documents.Paragraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    //move file, return the dest file name
    public static String moveFile(String src, String destDir) throws IOException {
        File srcFile = new File(src);
        String fileName = srcFile.getName();
        String parent = srcFile.getParent();
        Files.createDirectories(new File(destDir).toPath());
        srcFile.renameTo(new File(destDir + "/" + fileName));
        //文件移动后, 如果目录已空, 就删除目录
        File pFile = new File(parent);
        if (pFile.listFiles() == null || pFile.listFiles().length == 0)
            pFile.delete();
        return destDir + "/" + fileName;
    }

    public static Table findTableByKeyword(Document document, String keyword){
        for(Object obj : document.getSections().get(0).getTables()){
            Table table = (Table)obj;
            for(TableRow row:(Iterable<TableRow>)table.getRows()){
                for(TableCell cell : (Iterable<TableCell>)row.getCells()){
                    for(Paragraph para : (Iterable<Paragraph>)cell.getParagraphs()){
                        if (keyword.equals(para.getText().trim())){
                            return table;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void addTableRow(Table table, String[] values){
        TableRow row = table.addRow();
        for (int i = 0; i < values.length; i++) {
            row.getCells().get(i).addParagraph().setText(values[i]);
        }
    }

    /**
     *
     * @param table
     * @param prizeList
     */
    public static void addPrizeRow(Table table, List<? extends IPrize> prizeList){
        if (prizeList == null) prizeList = new ArrayList<>();
        prizeList.stream().forEach(p -> addTableRow(table,
                new String[]{String.valueOf(p.getSeq()), Constants.DFYMD.format(p.getDate()), p.getPrizeName(),
                        p.getPrizeLevel(), p.getPrizeDept()}));

        for (int i = 0; i < Constants.PRINT_RECCTABLE_PRIZE_ROW - prizeList.size(); i++) {
            table.addRow();//打印表格补齐最少行数, 避免数据太少样式不好看, 不够的, 就在后面增加空行
        }
    }

    public static void addSubsidizeRow(Table table, List<? extends ISubsidize> subsidizeList){
        if (subsidizeList == null) subsidizeList = new ArrayList<>();
        subsidizeList.stream().forEach(s -> addTableRow(table,
                new String[]{String.valueOf(s.getSeq()), Constants.DFYMD.format(s.getDate()),
                        s.getPlanNo()+ " " + s.getPlanName(),
                        s.getPlanType(), String.valueOf(s.getAmount())}));

        for (int i = 0; i < Constants.PRINT_RECCTABLE_SUBSIDIZE_ROW - subsidizeList.size(); i++) {
            table.addRow();
        }
    }

    public static void addExperienceRow(Table table, List<? extends IExperience> experienceList){
        if (experienceList == null) experienceList = new ArrayList<>();
        experienceList.stream().forEach(exp -> Utils.addTableRow(table,
                new String[]{Constants.DFYMD.format(exp.getStartDate()), Constants.DFYMD.format(exp.getEndDate()),
                        exp.getCompany(), exp.getTitle()}));

        for (int i = 0; i < Constants.PRINT_RECCTABLE_EXPERIENCE_ROW - experienceList.size(); i++) {
            table.addRow();
        }
    }
}
