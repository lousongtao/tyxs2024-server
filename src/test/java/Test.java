import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Test {

    public String moveFile(String src, String destPath) throws IOException {
        File file = new File(src);
        String fileName = file.getName();
        String parent = file.getParent();
        Files.createDirectories(new File(destPath).toPath());
        file.renameTo(new File(destPath + "/" + fileName));
        //文件移动后, 如果目录已空, 就删除目录
        File pFile = new File(parent);
        if (pFile.listFiles() == null || pFile.listFiles().length == 0)
            pFile.delete();
        return destPath + "/" + fileName;
    }

    public static void main(String[] args) throws IOException {
        String src = "H:\\Workspace-backup\\workspace-workscollect\\server\\fileupload_temp_savepath\\1638512508551\\TD deposit receipt.jpg";
        String destPath = ".\\worksfiles\\2";
        new Test().moveFile(src, destPath);
    }
}
