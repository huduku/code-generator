package cn.ucfgroup.createcode;

import java.util.Arrays;

/**
 * @author jrgc god zhou.
 * @version 1.0
 * @comment
 * @date 2016年3月28日 上午11:17:39
 * @qq 772716714
 */
public class Main {
    // 包名
    private final static String packageName = "com.himyidea.wfinance.asset.console";
    // 作者
    private final static String author = "huduokui";

    //private final static String author = "yangwentao";
    //QQ
    private final static String qq = "1359104621";
    //private final static String qq = "289505091";
    // 生成路径
    private final static String projectPath = "/home/hudk/resources/tmp/";
    //private final static String projectPath = "D:\\w\\console-parent";
    // 需生成表
    private final static String[] tables = {"test_user"};
    // 数据库0 oracle 1 mysql
    private final static int db = 1;

    public static void main(String[] args) {
        new CreateJava().createCode(packageName, author, qq, projectPath, Arrays.asList(tables), db, 1);
    }
}
