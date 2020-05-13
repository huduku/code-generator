package cn.ucfgroup.createcode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 
 * @author dev  2013-5-23
 * 
 */
public class Dom4jUtil {

	Logger logger = Logger.getLogger(Dom4jUtil.class);
	
	private static SAXReader reader = null;
	private static Dom4jUtil dom4jUtil = null;

	/**
	 * 构造方法
	 */
	private Dom4jUtil() {
		reader = new SAXReader();
		//reader.setEncoding("UTF-8");
	}

	/**
	 * 
	 * @return Dom4jUtil
	 */
	public static Dom4jUtil getInstantce() {
		if (dom4jUtil == null) {
			dom4jUtil = new Dom4jUtil();
		}
		return dom4jUtil;
	}

	/**
	 * 
	 * @param path 
	 * @return document
	 */
	public Document getDomByPath(String path) {
		Document doc = null;
		try {
			doc = reader.read(path);
		} catch (Exception e) {
			logger.error(e);
		}
		return doc;
	}

	/**
	 * 保存XML文件
	 */
	public void saveXMLFile(String filepath,Document document){
		OutputFormat format = OutputFormat.createPrettyPrint(); 
		format.setEncoding("utf-8"); 
		format.setIndent(true); 
		format.setNewlines(true);
		XMLWriter xmlWriter;
		try {
			xmlWriter = new XMLWriter(new FileOutputStream(filepath), format);
			xmlWriter.write(document);
			xmlWriter.flush();
			xmlWriter.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			xmlWriter = null;
		}
	}
}
