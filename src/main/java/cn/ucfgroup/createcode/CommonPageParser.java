package cn.ucfgroup.createcode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * <br>
 * <b>功能：</b>详细的功能描述<br>
 * <b>作者：</b>徐飞<br>
 * <b>日期：</b> 2011-7-22 <br>
 * <b>更新者：</b><br>
 * <b>日期：</b> <br>
 * <b>更新内容：</b><br>
 */
public class CommonPageParser {

    private VelocityEngine ve;
    private final static String CONTENT_ENCODING = "UTF-8";
    private static final Logger log = Logger.getLogger(CommonPageParser.class);
    private static boolean isReplace = true;  // 是否可以替换文件 true =可以替换，false =不可以替换

    public void setVe(VelocityEngine ve) {
        this.ve = ve;
    }

    public static void main(String[] args) throws IOException  
    {  
        /*File sourceFile = new File("E:/tmp");  
        File targetFile = new File("E:/tmp2");  
        copy(sourceFile, targetFile); */
    	System.out.println(System.getProperty("user.dir"));
  
    }  
    /**
     * 整个目录拷贝
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copy(File sourceFile, File targetFile) throws IOException  
    {      	
        if(sourceFile.exists()&&targetFile.exists())  
        {  
            File tarpath = new File(targetFile, sourceFile.getName());//创建新文件  
            if (sourceFile.isDirectory())//如果源文件是一个目录  
            {  
                tarpath.mkdir();//创建新目录  
                System.out.println("目录："+tarpath.getName()+"复制完成");  
                File[] dir = sourceFile.listFiles();//获取源文件下属的子文件  
                for(File f:dir)  
                {  
                    copy(f,tarpath);  
                }  
            }   
            else  
            {  
                //tarpath.createNewFile();//创建新文件  
                FileInputStream is=new FileInputStream(sourceFile);  
                FileOutputStream os=new FileOutputStream(tarpath);  
                BufferedInputStream bis=new BufferedInputStream(is);  
                BufferedOutputStream bos=new BufferedOutputStream(os);  
                int length=0;  
                byte []bytes=new byte[4096];  
                while((length=bis.read(bytes))!=-1)  
                {  
                    bos.write(bytes, 0, length);  
                }  
                if(bis!=null)  
                {  
                    bis.close();  
                }  
                if(bos!=null)  
                {  
                    bos.close();  
                }  
                System.out.println("文件："+tarpath.getName()+"复制完成");  
            }  
        }  
        else  
        {  
            System.out.println("源文件不存在 sourceFile.exists()="+sourceFile.exists() +" targetFile.exists()="+targetFile.exists());  
        }  
    }  
    /**
     * 文件拷贝
     * @param oldPath
     * @param newPath
     */
    public static void Copy(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("error  ");
			e.printStackTrace();
		}
	}
    /**
     * <br>
     * <b>功能：</b>生成页面文件<br>
     * <b>作者：</b>罗泽军<br>
     * <b>日期：</b> 2011-7-23 <br>
     * 
     * @param context 内容上下文
     * @param templateName 模板文件路径（相对路径）article\\article_main.html
     * @param targetPage 生成页面文件路径（相对路径） vowo\index_1.html
     */
    public void WriterPage(VelocityContext context, String templateName,
            String fileDirPath, String targetFile) {
        WriterPage(context, templateName, fileDirPath + targetFile);
    }

    /**
     * @param context 内容上下文
     * @param templateName 模板文件路径（相对路径）
     * @param filePath 生成文件路径
     */
    public void WriterPage(VelocityContext context, String templateName,
            String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                new File(file.getParent()).mkdirs();
            } else {
                if (isReplace) {
                    log.info("[replace]"+file.getAbsolutePath());
                } else {
                }
            }
            Template template = ve.getTemplate(templateName, CONTENT_ENCODING);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    fos, CONTENT_ENCODING));
            template.merge(context, writer);
            writer.flush();
            writer.close();
            fos.close();
            System.out.println("[ok] "+file.getAbsolutePath());
        } catch (Exception e) {
            log.error("templateName="+templateName+" filePath="+filePath,e);
        }
    }
}
