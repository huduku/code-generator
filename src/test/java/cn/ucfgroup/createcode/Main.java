package cn.ucfgroup.createcode;

import java.util.Arrays;

/**
 * 
 * @comment
 * @date 2016年3月28日 上午11:17:39
 * @author jrgc god zhou.
 * @qq 772716714
 * @version 1.0
 */
public class Main {
	// 包名
	private final static String packageName = "com.himyidea.wfinance.asset.console";
	// 作者
	private final static String author = "huduokui@ucfgroup.com";
	
	//private final static String author = "yangwentao";
	//QQ
	private final static String qq = "1359104621";
	//private final static String qq = "289505091";
	// 生成路径
	private final static String projectPath = "E:\\Resource\\works\\himyidea\\repo\\asset-console";
	//private final static String projectPath = "D:\\w\\console-parent";
	// 需生成表
	private final static String[] tables = { "ASSET_USER"};
	// 数据库0 oracle 1 mysql
	private final static int db = 1;

	public static void main(String[] args) {
		new CreateJava().creadcode(packageName,author,qq, projectPath, Arrays.asList(tables), db, 1);
	}
}
