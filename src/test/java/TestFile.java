import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestFile {
    public static void main(String[] args) throws IOException {
        String src = "H:\\Workspace-backup\\workspace-workscollect\\temppath\\1641511512518\\wf.pdf";
        String dest = "H:\\Workspace-backup\\workspace-workscollect\\WorksFiles\\31";
        File srcFile = new File(src);
        String fileName = srcFile.getName();
        String parent = srcFile.getParent();
        Files.createDirectories(new File(dest).toPath());
        srcFile.renameTo(new File(dest+"\\"+fileName));
        //文件移动后, 如果目录已空, 就删除目录
        File pFile = new File(parent);
        if (pFile.listFiles() == null || pFile.listFiles().length == 0)
            pFile.delete();
    }
}
