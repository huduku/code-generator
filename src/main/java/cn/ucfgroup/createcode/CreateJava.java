package cn.ucfgroup.createcode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;

/**
 * 
 * @comment
 * @date 2016年3月28日 下午1:42:43
 * @author jrgc god zhou.
 * @qq 772716714
 * @version 1.0
 */
public class CreateJava {

	private static final Logger log = Logger.getLogger(CreateJava.class);

	// 加载默认数据库配置。
	private static ResourceBundle res = ResourceBundle.getBundle("database");
	private static String driver = res.getString("gpt.driver");
	private static String url = res.getString("gpt.url");
	private static String userName = res.getString("gpt.username");
	private static String passWord = res.getString("gpt.password");
	private static String templateDefaultTemp = System.getProperty("java.io.tmpdir") + "templateDefaultTemp/";

	// 模板路径
	List<String> templatePaths = null;
	// 模板配置
	List<String> extendedConfigPaths = null;
	Connection connection = null;

	public CreateJava() {
		super();
		// 加载默认模板
		init();
	}

	/**
	 * 如果打成jar包。模板路径会有问题，把jar包中的模板拷贝到临时目录。
	 */
	private void init() {
		log.info("临时目录:" + templateDefaultTemp);
		templatePaths = new ArrayList<String>();
		extendedConfigPaths = new ArrayList<String>();

		ResourceBundle bundle = ResourceBundle.getBundle("defaultTemplate");
		if (bundle == null)
			return;
		Set<String> keySet = bundle.keySet();
		if (keySet == null || keySet.size() == 0)
			return;
		File folder = new File(templateDefaultTemp);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		ClassLoader classLoader = CreateJava.class.getClassLoader();
		for (String key : keySet) {
			URL tempUrl = classLoader.getResource(bundle.getString(key));
			if (tempUrl == null) {
				continue;
			}
			try {

				File file = new File(templateDefaultTemp + key);
				// if (file.exists())
				// continue;
				InputStream inputStream = tempUrl.openStream();
				OutputStream outputStream = new FileOutputStream(file);
				byte[] datas = new byte[1024];
				int l = -1;
				while ((l = inputStream.read(datas)) != -1) {
					outputStream.write(datas, 0, l);
				}
				inputStream.close();
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		templatePaths.add(templateDefaultTemp);
	}

	/**
	 * 生成数据库连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	Connection generateConnection() throws SQLException {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(url, userName, passWord);
	}

	/**
	 * 自动生成代码的主方法。
	 * 
	 * @param projectName
	 *            工程名
	 * @param author
	 *            作者     
     * @param qq
	 *            qq    
	 * @param projectPath
	 *            工程路径
	 * @param tableName
	 *            数据库表名
	 * @param dbType
	 *            数据库类型0：oracle 1：mysql
	 * @param buttonType
	 *            按钮类型0：英文 1：汉字
	 * @param createFileType是否成成系统配置文件
	 *            （pom文件，application-Temp-dao.xml，数据库配置文件。。。。）1生成 0不生成
	 */
	public void creadcode(String projectName,String author,String qq,String projectPath, List<String> tableList, int dbType, int buttonType) {
		if (CollectionUtils.isEmpty(tableList)) {
			throw new RuntimeException("表名为空，表名不能为空");
		}

		if (StringUtils.isBlank(projectPath)) {
			throw new RuntimeException("项目路径不能为空");
		}
		if (StringUtils.isBlank(projectName)) {
			throw new RuntimeException("项目名称不能为空");
		}

		for (String tableName : tableList) {
			VelocityContext context = new VelocityContext();
			context.put("buttonType", buttonType);
			if (getConnection() == null) {
				try {
					this.setConnection(generateConnection());
				} catch (SQLException e1) {
					e1.printStackTrace();
					throw new RuntimeException("数据库连接异常:uri=" + url + ",userName=" + userName + ",passWord=" + passWord);
				}
			}
			CreateBean createBean = new CreateBean();
			createBean.setConnection(this.getConnection());
			try {
				this.getInfosFromTable(context, createBean, tableName, dbType);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("获取表信息错误");
			}
			try {
				this.putOtherContextInfo(context, createBean, projectName, tableName, dbType);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException("获取表注释信息错误");
			}

			context = getExtendedVelocityContext(context);

			this.putPathContextInfo(context, projectName,author,qq, projectPath);
			this.createFiles(context, projectPath);
		}
		try {
			getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//执行清理工作
		FileUtil.deleteDir(new File(templateDefaultTemp));
	}

	/**
	 * 从数据库中查询模板中需要的参数信息。
	 * 
	 * @param context
	 *            上下文
	 * @param createBean
	 *            代码生成bean
	 * @param tableName
	 *            表名
	 * @throws Exception
	 *             数据库查询异常
	 */
	void getInfosFromTable(VelocityContext context, CreateBean createBean, String tableName, int dbType) throws Exception {
		List<ColumnData> columnDatas = createBean.getColumnDatas(tableName, dbType);
		context.put("columnDatas", columnDatas); // 生成bean
		/****************************** 生成bean字段 *********************************/
		context.put("feilds", createBean.getBeanFeilds(columnDatas)); // 生成bean
		
		context.put("superFeilds", createBean.getBeanSuperFeilds(columnDatas));

		//判断相应属性
		createBean.initParam(context,columnDatas);
		/******************************* 生成sql语句 **********************************/
		Map<String, Object> sqlMap = createBean.getAutoCreateSql(tableName, dbType);
		context.put("SQL", sqlMap);
		/******************************* 生成和删除字段相关的代码 **********************************/
		context.put("DELETED_OPERATION", "");
		context.put("DELETED_OPERATION", "");
		for (ColumnData columnData : columnDatas) {
			if ("DELETED".equalsIgnoreCase(columnData.getColumnName())) {
				context.put("DELETED_OPERATION", "bean.setDeleted(DELETED.NO.key);");
				return;
			}
		}

	}

	/**
	 * 把自动化的其他常用变量放在上下文中
	 * 
	 * @param context
	 *            上下文
	 * @param createBean
	 *            代码生成bean
	 * @param projectName
	 *            工程名
	 * @param tableName
	 *            表名
	 * @param dbType
	 *            数据库类型0：oracle 1：mysql
	 * @throws SQLException
	 *             数据库查询异常
	 */
	void putOtherContextInfo(VelocityContext context, CreateBean createBean, String projectName, String tableName, int dbType) throws SQLException {
		String className = createBean.getTablesNameToClassName(tableName);
		String lowerName = className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
		String upperName = className.substring(0, 1).toUpperCase() + className.substring(1, className.length());
		// 从数据库中查询表的注释
		String commentName = createBean.querryTableComment(tableName, dbType);
		context.put("projectName", projectName);
		context.put("className", className);
		context.put("lowerName", lowerName);
		context.put("upperName", upperName);
		context.put("commentName", commentName);
		context.put("tableName", tableName);
		context.put("formName", tableName.toLowerCase().replace("_", "-"));
		context.put("dbType", dbType);
		context.put("url", url);
		context.put("passWord", passWord);
		context.put("userName", userName);
		if (dbType == 0) {
			context.put("driverClass", "oracle.jdbc.driver.OracleDriver");
		} else {
			context.put("driverClass", "com.mysql.jdbc.Driver");
		}

		try {
			context.put("sysdateStr", DateUtil.getNowPlusTimeMill());
		} catch (Exception e) {
			e.printStackTrace();
		}
		context.put("seqName", "SEQ_" + tableName.toUpperCase());
		context.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

	}

	/**
	 * 添加扩展属性配置到上下文。利用addExtendedConfigPath方法添加的配置文件，可以添加到context中
	 * 
	 * @param context
	 *            上下文
	 * @return
	 */
	VelocityContext getExtendedVelocityContext(VelocityContext context) {
		for (String path : extendedConfigPaths) {
			Properties properties = null;
			try {
				properties = VelocityEngineFactory.loadProperties(path, "utf-8");
			} catch (Exception e) {
				log.info("扩展配置出错", e);
			}
			if (properties != null)
				context = new VelocityContext(properties, context);
		}
		return context;
	}

	/**
	 * 默认模板生成文件路径的处理
	 * 
	 * @param context
	 *            上下文
	 * @param projectName
	 *            工程名
	 * @param projectPath
	 *            工程路径
	 */
	void putPathContextInfo(VelocityContext context, String projectName,String author,String qq, String projectPath) {
		if (!(projectPath.endsWith("\\") || projectPath.endsWith("/"))) {
			projectPath = projectPath + "\\";
		}
		if (projectName.contains(".")) {
			projectName = projectName.replace(".", "/");
		}
		
		//注释
		context.put("author", author);
		context.put("qq", qq);

		// dao层
		String daoPath = projectPath + "\\console\\";
		context.put("dao.mapper.path", daoPath + "console-dao\\src\\main\\resources\\dal\\sqlmap\\" + context.get("lowerName") + "-mapper.xml");
		context.put("dao.do.path", daoPath + "console-dao\\src\\main\\java\\"+projectName+"\\data\\" + context.get("className") + "DO.java");
		context.put("dao.view.path", daoPath + "console-dao\\src\\main\\java\\"+projectName+"\\data\\view\\" + context.get("className") + "View.java");
		context.put("dao.dao.path", daoPath + "console-dao\\src\\main\\java\\"+projectName+"\\dao\\" + context.get("className") + "DAO.java");
		context.put("dao.daoimpl.path", daoPath + "console-dao\\src\\main\\java\\"+projectName+"\\dao\\impl\\" + context.get("className") + "DAOImpl.java");

		//config xml
//		XmlUtil.AddElementForDAO(daoPath + "console-dao\\src\\main\\resources\\dal\\persistence.xml", context.get("lowerName")+"DAO",context.get("projectName")+".dao.impl."+context.get("className")+"DAOImpl", context.get("commentName")+"", null);
//		XmlUtil.AddElementForMapper(daoPath + "console-dao\\src\\main\\resources\\dal\\mybatis-config.xml", context.get("className")+"DO",context.get("projectName")+".data."+context.get("className")+"DO", "dal/sqlmap/"+context.get("lowerName")+"-mapper.xml", null);

		File directory = new File("");// 参数为空
		String rootPath="";
		try {
			rootPath = directory.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		XmlUtil.AddElementForDAO(rootPath+"\\src\\test\\resources\\defaultTemplate\\persistence.xml", context.get("lowerName")+"DAO",context.get("projectName")+".dao.impl."+context.get("className")+"DAOImpl", context.get("commentName")+"", daoPath + "console-dao\\src\\main\\resources\\dal\\persistence.xml");
		XmlUtil.AddElementForMapper(rootPath+"\\src\\test\\resources\\defaultTemplate\\mybatis-config.xml", context.get("className")+"DO",context.get("projectName")+".data."+context.get("className")+"DO", "dal/sqlmap/"+context.get("lowerName")+"-mapper.xml", daoPath + "console-dao\\src\\main\\resources\\dal\\mybatis-config.xml");

		//service层
		context.put("service.service.path", daoPath + "console-service\\src\\main\\java\\"+projectName+"\\service\\" + context.get("className") + "Service.java");
		context.put("service.serviceimpl.path", daoPath + "console-service.impl\\src\\main\\java\\"+projectName+"\\service\\impl\\" + context.get("className") + "ServiceImpl.java");

		//controller层
		context.put("controller.controller.path", daoPath + "console-controller\\src\\main\\java\\"+projectName+"\\controllers\\" + context.get("className") + "Controller.java");

		//resource层
		context.put("jsBarVm", "#parse(\"main-js-bar.vm\")");
		context.put("messageVm", "#parse(\"message.vm\")");
		context.put("navigationBarVm", "#parse(\"top-navigation.vm\")");
		context.put("resource.html.path", projectPath + "console-resources\\src\\main\\webapp\\pages\\" + context.get("lowerName")+"\\"+context.get("lowerName")+"List.vm");
		context.put("resource.js.path", projectPath + "console-resources\\src\\main\\webapp\\dist\\js\\pages\\" + context.get("lowerName")+"\\"+context.get("lowerName")+"List.js");
		
		context.put("resource.validator.js.path", projectPath + "console-resources\\src\\main\\webapp\\dist\\js\\pages\\" + context.get("lowerName")+"\\"+context.get("lowerName")+"List.validator.js");
		
		context.put("top.navigation.html.path", projectPath + "console-resources\\src\\main\\webapp\\pages\\top-navigation.vm");
		context.put("top.navigation.js.path", projectPath + "console-resources\\src\\main\\webapp\\dist\\js\\pages\\top-navigation.js");
	}

	/**
	 * 根据上下文自动生成文件
	 * 
	 * @param context
	 *            上下文
	 * @param createType
	 *            （某些表属于字典表，不需要生成jsp相关）1生成，0不生成
	 */
	void createFiles(VelocityContext context, String projectPath) {
		CommonPageParser commonPageParser = new CommonPageParser();
		commonPageParser.setVe(VelocityEngineFactory.generateVelocityEngine(templatePaths.toArray(new String[templatePaths.size()])));
		
		//dao
		commonPageParser.WriterPage(context, "dao.mapper", (String) context.get("dao.mapper.path"));
		commonPageParser.WriterPage(context, "dao.do", (String) context.get("dao.do.path"));
		commonPageParser.WriterPage(context, "dao.view", (String) context.get("dao.view.path"));
		commonPageParser.WriterPage(context, "dao.dao", (String) context.get("dao.dao.path"));
		commonPageParser.WriterPage(context, "dao.daoimpl", (String) context.get("dao.daoimpl.path"));
		
		//service
		commonPageParser.WriterPage(context, "service.service", (String) context.get("service.service.path"));
		commonPageParser.WriterPage(context, "service.serviceimpl", (String) context.get("service.serviceimpl.path"));
		
		//controller
		commonPageParser.WriterPage(context, "controller.controller", (String) context.get("controller.controller.path"));
		
		//resource
		commonPageParser.WriterPage(context, "top.navigation.html", (String) context.get("top.navigation.html.path"));
		commonPageParser.WriterPage(context, "top.navigation.js", (String) context.get("top.navigation.js.path"));
		
		commonPageParser.WriterPage(context, "resource.html", (String) context.get("resource.html.path"));
		commonPageParser.WriterPage(context, "resource.js", (String) context.get("resource.js.path"));
		commonPageParser.WriterPage(context, "resource.validator.js", (String) context.get("resource.validator.js.path"));
		
		
	}

	/**
	 * 设置生成代码的数据库连接
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	public void setConnection(String driver, String url, String userName, String passWord) throws SQLException {
		CreateJava.driver = driver;
		CreateJava.url = url;
		CreateJava.userName = userName;
		CreateJava.passWord = passWord;
		this.connection = generateConnection();
	}

	/**
	 * 设置生成代码的数据库连接
	 * 
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * 添加自定义模板路径和其相关的配置
	 * 
	 * @param templatePath
	 *            模板路径
	 * @param extendedConfigPath
	 *            扩展配置文件路径
	 */
	public void addTemplatePath(String templatePath, String extendedConfigPath) {
		templatePaths.add(templatePath);
		if (!StringUtils.isBlank(extendedConfigPath))
			extendedConfigPaths.add(extendedConfigPath);
	}

	/**
	 * 添加自定义模板扩展配置
	 * 
	 * @param extendedConfigPath
	 *            扩展配置文件路径
	 */
	public void addExtendedConfigPath(String extendedConfigPath) {
		if (!StringUtils.isBlank(extendedConfigPath))
			extendedConfigPaths.add(extendedConfigPath);
	}

	/**
	 * 获得连接
	 * 
	 * @return
	 */
	Connection getConnection() {
		return connection;
	}

}
