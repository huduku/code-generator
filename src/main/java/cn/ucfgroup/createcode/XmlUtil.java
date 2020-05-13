package cn.ucfgroup.createcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * @comment
 * @date 2016年3月28日 下午3:32:18
 * @author zhouxiao
 * @qq 772716714
 * @version 1.0
 */
public class XmlUtil {

	/**
	 * 写入dao xml for persistence.xml
	 * 
	 * @param xmlPath
	 * @param id
	 * @param cls
	 * @param description
	 * @param outPath
	 */
	public static void AddElementForDAO(String xmlPath, String id, String cls, String description, String outPath) {
		try {
			if (outPath == null)
				outPath = xmlPath;
			File file=new File(outPath);
			if(file.exists()) xmlPath=outPath;
			else file.createNewFile();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new FileInputStream(xmlPath));
			Element root = doc.getRootElement();
			root.addComment(description);
			Element element = root.addElement("bean");
			element.setAttributeValue("id", id);
			element.setAttributeValue("class", cls);
			Element element2 = element.addElement("property");
			element2.setAttributeValue("name", "sqlSessionTemplate");
			element2.setAttributeValue("ref", "sqlSession");
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(outPath), format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
		//	e.printStackTrace();
		}
	}

	/**
	 * 写入 mapper for mybatis-config.xml
	 * @param xmlPath
	 * @param doAlias
	 * @param doType
	 * @param mapper
	 * @param outPath
	 */
	public static void AddElementForMapper(String xmlPath, String doAlias, String doType, String mapper, String outPath) {
		try {
			if (outPath == null)
				outPath = xmlPath;
			File file=new File(outPath);
			if(file.exists()) xmlPath=outPath;
			else file.createNewFile();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new FileInputStream(xmlPath));
			Element root = doc.getRootElement();
			Element e1 = root.element("typeAliases");
			Element element1 = e1.addElement("typeAlias");
			element1.setAttributeValue("alias", doAlias);
			element1.setAttributeValue("type", doType);
			Element e2 = root.element("mappers");
			Element element2 = e2.addElement("mapper");
			element2.setAttributeValue("resource", mapper);
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(new FileWriter(outPath), format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
	//		e.printStackTrace();
		}
	}
}
