package cn.ucfgroup.createcode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;

/**
 * <br>
 * <b>功能：</b>1、数据库相关操作。2、模板中一些较长变量的生成。<br>
 * <b>作者：</b>肖财高<br>
 * <b>日期：</b> 2011-12-16 <br>
 * <b>版权所有：<b>版权所有(C) 2011，QQ 405645010<br>
 */
public class CreateBean {

    // 数据库连接
    private Connection connection = null;
    static String rt = "\r\t";

    // sql（查询所有的表）
    String SQLTables = "show tables";

    public static boolean isUUID=false;

    // public void setMysqlInfo(String url, String username, String password) {
    // this.url = url;
    // this.username = username;
    // this.password = password;
    // }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * 查询数据库中的表
     * 
     * @return
     * @throws SQLException
     */
    public List<String> getTables() throws SQLException {
        Connection con = this.getConnection();
        PreparedStatement ps = con.prepareStatement(SQLTables);
        ResultSet rs = ps.executeQuery();
        List<String> list = new ArrayList<String>();
        while (rs.next()) {
            String tableName = rs.getString(1);
            list.add(tableName);
        }
        rs.close();
        ps.close();
        // con.close();
        return list;
    }

    /**
     * 获得表注释，用于生成java代码注释
     * 
     * @param tableName
     * @param dbType 数据库类型0：oracle  1：mysql
     * @throws SQLException
     */
    protected String querryTableComment(String tableName,int dbType) throws SQLException {
    	
    	String tableCommentSql = null;
    	if(dbType==0){
    		// oracle
            tableCommentSql =
            "select * from user_tab_comments where table_name='"
            + tableName.toUpperCase() + "'";
    	}else{
    		// mysql
            tableCommentSql = " SELECT TABLE_COMMENT COMMENTS FROM INFORMATION_SCHEMA.TABLES  WHERE  table_name = '"
                    + tableName.toUpperCase() + "'";
    	}            	       
        Connection con = this.getConnection();
		PreparedStatement ps = con.prepareStatement(tableCommentSql);
        ResultSet rs = ps.executeQuery();
        rs.next();
        String comment = rs.getString("COMMENTS");
        rs.close();
        ps.close();
        // con.close();
        if (StringUtils.isBlank(comment)) {
            return tableName;
        } else {
            return comment;
        }
    }

    /**
     * 查询表的字段，封装成List
     * 
     * @param tableName
     * @param dbType 数据库类型0：oracle  1：mysql
     * @return
     * @throws SQLException
     */
    public List<ColumnData> getColumnDatas(String tableName,int dbType)
            throws SQLException {
    	
    	String SQLColumns = null;
    	String pkSQL = null;
    	if(dbType==0){
    		//Oracle
            SQLColumns =
            "select t1.COLUMN_NAME,t1.DATA_TYPE,t2.COMMENTS,t1.DATA_SCALE"
            + " from user_tab_columns  t1 left outer join user_col_comments  t2"
            +
            " on t1.COLUMN_NAME=t2.COLUMN_NAME and t1.TABLE_NAME=t2.TABLE_NAME  where t1.TABLE_NAME='"
            + tableName.toUpperCase() + "' ";
           
            
            //Oracle
            pkSQL = "select cu.column_name  " +
            		"from user_cons_columns cu, user_constraints au " +
            		"where cu.constraint_name = au.constraint_name   and au.constraint_type = 'P'   and au.table_name = '"+ 
            		tableName.toUpperCase() + "' ";
    	}else{
    		// mysql
            SQLColumns = "SELECT COLUMN_NAME , DATA_TYPE , COLUMN_COMMENT ,CHARACTER_MAXIMUM_LENGTH DATA_SCALE "
                    + " from INFORMATION_SCHEMA.COLUMNS "
                    + " WHERE table_name = '"
                    + tableName.toUpperCase() + "'";
            //mysql
            pkSQL = "SELECT c.COLUMN_NAME " +
            		"FROM  INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS t,   INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS c " +
            		"WHERE  t.TABLE_NAME = c.TABLE_NAME    AND t.CONSTRAINT_TYPE = 'PRIMARY KEY'  AND t.TABLE_SCHEMA = '"+ 
            		tableName.toUpperCase() + "' ";
    	}
        
         
         
    	
   //     System.out.println(SQLColumns);
        
        String pkColumn = null;
        Connection con = this.getConnection();
        PreparedStatement pkPs = con.prepareStatement(pkSQL);
        ResultSet pkrs = pkPs.executeQuery();
        if(pkrs.next()){
        	pkColumn = pkrs.getString(1);
        }
        
        PreparedStatement ps = con.prepareStatement(SQLColumns);
        List<ColumnData> columnList = new ArrayList<ColumnData>();
        ResultSet rs = ps.executeQuery();
        StringBuffer str = new StringBuffer();
        StringBuffer getset = new StringBuffer();
        while (rs.next()) {
            String name = rs.getString(1);
            String type = rs.getString(2);
            String comment = rs.getString(3);
            Object o = rs.getObject(4);
            int scale = 0;
            if (o != null && !o.equals("")) {
                scale = rs.getInt(4);
            }

            String javaType = this.getJavaType(type, scale);
            String jdbcType = this.getJdbcType(type, scale);

            ColumnData cd = new ColumnData();
            cd.setColumnName(name.toLowerCase());
            cd.setAttrName(getFeildsNameTo(cd.getColumnName()));
            String attrName = cd.getAttrName();
            cd.setFirstUpperAttrName(attrName.substring(0, 1).toUpperCase()+attrName.substring(1, attrName.length()));
            cd.setDataType(javaType);
            cd.setColumnComment(comment);
            cd.setScale(scale);
            cd.setJdbcType(jdbcType);
            if (pkColumn!=null && pkColumn.equalsIgnoreCase(cd.getColumnName())) {//主键放第一个位置
                columnList.add(0, cd);
            } else {
                columnList.add(cd);
            }
        }
        argv = str.toString();
        method = getset.toString();
        rs.close();
        ps.close();
        // con.close();
        return columnList;
    }

    private String method;
    private String argv;

    /**
     * 生成实体Bean 的属性和set,get方法
     * 
     * @param tableName
     * @return
     * @throws SQLException
     */
    public String getBeanFeilds(List<ColumnData> dataList) throws SQLException {
        StringBuffer str = new StringBuffer();
        StringBuffer getset = new StringBuffer();
        for (ColumnData d : dataList) {
            String name = d.getAttrName();
            String type = d.getDataType();
            String comment = d.getColumnComment();
            // type=this.getType(type);
            String maxChar = name.substring(0, 1).toUpperCase();
            str.append("\r\t").append("// ").append(comment);
            str.append("\r\t").append("private ").append(type + " ")
                    .append(name).append(";");
            String method = maxChar + name.substring(1, name.length());
            getset.append("\r\t").append("/**\r\t *\r\t * @return the ")
                    .append(name).append("\r\t */");
            getset.append("\r\t").append("public ").append(type + " ")
                    .append("get" + method + "() {\r\t");
            getset.append("    return this.").append(name).append(";}\r\t");
            getset.append("/**\r\t *\r\t * @param ").append(name)
                    .append(" the ").append(name).append(" to set")
                    .append("\r\t */");
            
            getset.append("\r\t")
            .append("public void ")
            .append("set" + method + "(" + type + " " + name
                    + ") {\r\t");
        	getset.append("    this." + name + "=").append(name)
            .append(";\r\t}");
   /**         if(type.equals("java.util.Date")){
            	getset.append("\r\t")
            	.append("@JsonSerialize(using = CustomDateSerializer.class) \r\t")
                .append("public void ")
                .append("set" + method + "(" + type + " " + name
                        + ") {\r\t");
            	getset.append("    this." + name + "=").append(name)
                .append(";\r\t}");
            }else{
            	getset.append("\r\t")
                .append("public void ")
                .append("set" + method + "(" + type + " " + name
                        + ") {\r\t");
            	getset.append("    this." + name + "=").append(name)
                .append(";\r\t}");
            }
  **/          
        }
        argv = str.append("\r\t").toString();
        method = getset.toString();
        return argv + method;
    }
    
    public void initParam(VelocityContext context,List<ColumnData> dataList) throws SQLException {
    	context.put("isUUID", false);
    	context.put("isCreateTime", false);
    	context.put("isUpdateTime", false);
        for (ColumnData d : dataList) {
            String name = d.getAttrName();
            if("uuid".equals(name.toLowerCase())) {
            	context.put("isUUID", true);
            	isUUID=true;
            }
            else if("createtime".equals(name.toLowerCase()) || "createTime".equals(name.toLowerCase())) context.put("isCreateTime", true);
            else if("updatetime".equals(name.toLowerCase()) || "updateTime".equals(name.toLowerCase())) context.put("isUpdateTime", true);
        }
    }
    
    /**
     * 生成实体Bean 的属性和set,get方法
     * 
     * @param tableName
     * @return
     * @throws SQLException
     */
    public String getBeanSuperFeilds(List<ColumnData> dataList) throws SQLException {
        StringBuffer str = new StringBuffer();
        StringBuffer getset = new StringBuffer();
        for (ColumnData d : dataList) {
            String name = d.getAttrName();
            if("id".equals(name) || "uuid".equals(name)) continue;
            String type = d.getDataType();
            String comment = d.getColumnComment();
            // type=this.getType(type);
            String maxChar = name.substring(0, 1).toUpperCase();
            str.append("\r\t").append("// ").append(comment);
            str.append("\r\t").append("private ").append(type + " ")
                    .append(name).append(";");
            String method = maxChar + name.substring(1, name.length());
            getset.append("\r\t").append("/**\r\t *\r\t * @return the ")
                    .append(name).append("\r\t */");
            getset.append("\r\t").append("public ").append(type + " ")
                    .append("get" + method + "() {\r\t");
            getset.append("    return this.").append(name).append(";}\r\t");
            getset.append("/**\r\t *\r\t * @param ").append(name)
                    .append(" the ").append(name).append(" to set")
                    .append("\r\t */");
            
            getset.append("\r\t")
            .append("public void ")
            .append("set" + method + "(" + type + " " + name
                    + ") {\r\t");
        	getset.append("    this." + name + "=").append(name)
            .append(";\r\t}");
      
        }
        argv = str.append("\r\t").toString();
        method = getset.toString();
        return argv + method;
    }

    /**
     * <br>
     * <b>功能：</b>详细的功能描述<br>
     * <b>作者：</b>肖财高<br>
     * <b>日期：</b> 2011-12-20 <br>
     * 
     * @param tableName
     * @return
     * @throws SQLException
     */
    // public List<Map<String,String>> getColumnsMap(String tableName) throws
    // SQLException{
    // String
    // SQLColumns="SELECT distinct COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT FROM information_schema.columns WHERE  table_schema = 'ssi' and table_name =  '"+tableName+"' ";
    // // String SQLColumns="desc "+tableName;
    // Connection con=this.getConnection();
    // PreparedStatement ps=con.prepareStatement(SQLColumns);
    // List<Map<String,String>> listMap=new ArrayList<Map<String,String>>();
    // ResultSet rs=ps.executeQuery();
    // while(rs.next()){
    // Map<String,String> columnsMap=new HashMap<String, String>();
    // // String name = rs.getString(1);
    // // String type = rs.getString(2);
    // // String comment = rs.getString(3);
    //
    //
    // String name = rs.getString(1);
    // String type = rs.getString(2);
    // String comment = rs.getString(3);
    // type=this.getType(type);
    // columnsMap.put("COLUMN_NAME", name);
    // columnsMap.put("DATA_TYPE", type);
    // columnsMap.put("COLUMN_COMMENT", comment);
    // listMap.add(columnsMap);
    // }
    // rs.close();
    // ps.close();
    // con.close();
    // return listMap;
    // }
    public String getJdbcType(String type, int scale) {
        type = type.toLowerCase();
        if ("char".equals(type) || "varchar".equals(type)
                || "varchar2".equals(type) || "nvarchar2".equals(type)) {
            return "VARCHAR";
        }
        else if ("binary_double".equals(type)) {
            return "BINART_DOUBLE";
        }
        else if ("binary_float".equals(type)) {
            return "BINARY_FLOAT";
        }
        else if ("int".equals(type)) {
            return "INTEGER";
        } else if ("float".equals(type)) {
            return "Float";
        } else if ("bigint".equals(type)) {
            return "BIGINT";
        } else if ("number".equals(type)||"double".equals(type)) {
            return scale > 0 ? "DOUBLE" : "INTEGER";
        } else if ("date".equals(type) || "datetime".equals(type)) {
            return "DATE";
        } else if (type != null && type.startsWith("timestamp")) {
            return "TIMESTAMP";
        } else if (type != null && type.equals("clob")) {
            return "CLOB";
        }else if ("tinyint".equals(type) ||"TINYINT".equals(type)) {//mysql特有
            return "BOOLEAN";
        }
        return null;
    }

    public String getJavaType(String type, int scale) {
        type = type.toLowerCase();
        if ("char".equals(type) || "varchar".equals(type)
                || "varchar2".equals(type) || "nvarchar2".equals(type)) {
            return "String";
        }
        else if ("binary_double".equals(type)) {
            return "Double";
        }
        else if ("binary_float".equals(type)) {
            return "Float";
        }
        else if ("int".equals(type)) {
            return "Integer";
        } else if ("float".equals(type)) {
            return "Float";
        } else if ("bigint".equals(type)) {
            return "Long";
        } else if ("number".equals(type)||"double".equals(type)) {
            return scale > 0 ? "Double" : "Integer";
        } else if ((type != null && type.startsWith("timestamp"))
                || "date".equalsIgnoreCase(type)
                || "datetime".equalsIgnoreCase(type)) {
            return "java.util.Date";
        } else if ("clob".equals(type)) {
            return "String";
        }else if ("tinyint".equals(type) ||"TINYINT".equals(type)) {//mysql特有
            return "Boolean";
        }
        return null;
    }

    public void getPackage(int type, String createPath, String content,
            String packageName, String className, String extendsClassName,
            String... importName) throws Exception {
        if (null == packageName) {
            packageName = "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("package ").append(packageName).append(";\r");
        sb.append("\r");
        for (int i = 0; i < importName.length; i++) {
            sb.append("import ").append(importName[i]).append(";\r");
        }
        sb.append("\r");
        sb.append("/**\r *  entity. @author wolf Date:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date()) + "\r */");
        sb.append("\r");
        sb.append("\rpublic class ").append(className);
        if (null != extendsClassName) {
            sb.append(" extends ").append(extendsClassName);
        }
        if (type == 1) { // bean
            sb.append(" ").append("implements java.io.Serializable {\r");
        } else {
            sb.append(" {\r");
        }
        sb.append("\r\t");
        sb.append("private static final long serialVersionUID = 1L;\r\t");
        String temp = className.substring(0, 1).toLowerCase();
        temp += className.substring(1, className.length());
        if (type == 1) {
            sb.append("private " + className + " " + temp + "; // entity ");
        }
        sb.append(content);
        sb.append("\r}");
        System.out.println(sb.toString());
        this.createFile(createPath, "", sb.toString());
    }

    /**
     * <br>
     * <b>功能：</b>表名转换成类名 每_首字母大写<br>
     * <b>作者：</b>肖财高<br>
     * <b>日期：</b> 2011-12-21 <br>
     * 
     * @param tableName
     * @return
     */
    public String getTablesNameToClassName(String tableName) {
        String[] split = tableName.split("_");
        if (split.length > 1) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String tempTableName = split[i].substring(0, 1).toUpperCase()
                        + split[i].substring(1, split[i].length())
                                .toLowerCase();
                sb.append(tempTableName);
            }
            return sb.toString();
        } else {
            String tempTables = split[0].substring(0, 1).toUpperCase()
                    + split[0].substring(1, split[0].length());
            return tempTables;
        }
    }

    /**
     * <br>
     * <b>功能：</b>表字段名 转换驼峰命令（ _ 分割）<br>
     * 
     * @param feildName
     * @return
     */
    public String getFeildsNameTo(String feildName) {
        String[] split = feildName.split("_");
        if (split.length > 1) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String tempTableName = null;
                if (i == 0) {
                    tempTableName = split[i];
                } else {
                    tempTableName = split[i].substring(0, 1).toUpperCase()
                            + split[i].substring(1, split[i].length());
                }
                sb.append(tempTableName);
            }
            return sb.toString();
        } else {
            return feildName;
        }
    }

    /**
     * <br>
     * <b>功能：</b>创建文件<br>
     * <b>作者：</b><br>
     * <b>日期：</b> 2011-12-21 <br>
     * 
     * @param path
     * @param fileName
     * @param str
     * @throws IOException
     */
    public void createFile(String path, String fileName, String str)
            throws IOException {
        FileWriter writer = new FileWriter(new File(path + fileName));
        writer.write(new String(str.getBytes("utf-8")));
        writer.flush();
        writer.close();
    }

    /**
     * <br>
     * <b>功能：</b>生成sql语句<br>
     * <b>作者：</b><br>
     * <b>日期：</b> 2011-12-21 <br>
     * 
     * @param tableName
     * @throws Exception
     */
    static String selectStr = "select ";
    static String from = " from ";

    public Map<String, Object> getAutoCreateSql(String tableName,int dbType)
            throws Exception {
        return getAutoCreateSql(tableName, getColumnDatas(tableName,dbType),dbType);
    }

    public Map<String, Object> getAutoCreateSql(String tableName,
            List<ColumnData> columnDatas,int dbType) throws Exception {
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        if (columnDatas == null)
            columnDatas = getColumnDatas(tableName,dbType);
        String columns = this.getColumnSplit(columnDatas);
        String columnsAndJdbcType = this.getColumnAndJdbcTypeSplit(columnDatas);
        String[] columnList = getColumnList(columns);  // 表所有字段
        String[] columnAndJdbcTypeList = getColumnList(columnsAndJdbcType);
        String columnFields = getColumnFields(columns); // 表所有字段 按","隔开
        String insert = "insert into " + tableName + "("
                + columns.replaceAll("\\|", ",") + ")\n values(#{"
                + columnsAndJdbcType.replaceAll("\\|", "},#{") + "})";
        String update = getUpdateSql(tableName, columnAndJdbcTypeList,columnDatas);
        String updateSelective = getUpdateSelectiveSql(tableName, columnDatas);
        String selectById = getSelectByIdSql(tableName, columnAndJdbcTypeList,columnDatas);
        String delete = getDeleteSql(tableName, columnAndJdbcTypeList,columnDatas);
        sqlMap.put("columnList", columnList);
        sqlMap.put("columnFields", columnFields);
        sqlMap.put("insert",
                insert.replace("#{createTime,jdbcType=DATE}", "#{createTime,jdbcType=TIMESTAMP}")
                        .replace("#{updateTime,jdbcType=DATE}", "#{updateTime,jdbcType=TIMESTAMP}"));
        sqlMap.put("update",
                update.replace("#{createTime,jdbcType=DATE}", "#{createTime,jdbcType=TIMESTAMP}")
                .replace("#{updateTime,jdbcType=DATE}", "#{updateTime,jdbcType=TIMESTAMP}"));
        sqlMap.put("delete", delete);
        sqlMap.put("updateSelective", updateSelective);
        sqlMap.put("selectById", selectById);
        return sqlMap;
    }

    /**
     * delete
     * 
     * @param tableName
     * @param columnsList
     * @return
     * @throws SQLException
     */
    public String getDeleteSql(String tableName, String[] columnsList,List<ColumnData> columnList)
            throws SQLException {
        List<String> columns = Arrays.asList(columnsList);
        StringBuffer sb = new StringBuffer();
     
        	String s=columnList.get(0).getColumnName();
        	if(CreateBean.isUUID) s="UUID";
            sb.append("delete ");
            sb.append("\t from ").append(tableName).append(" where ");
            sb.append(s).append(" in  <foreach item=\"ITEM\" collection=\"array\" open=\"(\" separator=\",\" close=\")\">  #{ITEM} </foreach>");
        
        return sb.toString();
    }

    /**
     * 根据id查询
     * 
     * @param tableName
     * @param columnsList
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unused")
    public String getSelectByIdSql(String tableName, String[] columnsList,List<ColumnData> columnList)
            throws SQLException {
        StringBuffer sb = new StringBuffer();
        String[] ids = columnsList[0].split(",");
        sb.append("select <include refid=\"baseColumnList\" /> \n");
        sb.append("\t from ").append(tableName).append(" where ");
        sb.append(columnList.get(0).getColumnName()).append(" = #{")
                .append(columnsList[0]).append("}");
        return sb.toString();
    }

    /**
     * 获取所有的字段，并按","分割
     * 
     * @param columns
     * @return
     * @throws SQLException
     */
    public String getColumnFields(String columns) throws SQLException {
        String fields = columns;
        if (fields != null && !"".equals(fields)) {
            fields = fields.replaceAll("[|]", ",");
        }
        return fields;
    }

    /**
     * @param columns
     * @return
     * @throws SQLException
     */
    public String[] getColumnList(String columns) throws SQLException {
        String[] columnList = columns.split("[|]");
        return columnList;
    }

    /**
     * 修改记录
     * 
     * @param tableName
     * @param columnsList
     * @return
     * @throws SQLException
     */
    public String getUpdateSql(String tableName, String[] columnsList,List<ColumnData> columnList)
            throws SQLException {
        StringBuffer sb = new StringBuffer();
        String idStr = columnsList[0];
        String[] ids = idStr.split(",");
        for (int i = 1; i < columnsList.length; i++) {
            String[] column = columnsList[i].split(",");
            if ("CREATETIME".equals(column[0].toUpperCase()))
                continue;

            if ("UPDATETIME".equals(column[0].toUpperCase()))
                sb.append(columnList.get(i).getColumnName() + "=#{updateTime,jdbcType=TIMESTAMP}");
            else
                sb.append(columnList.get(i).getColumnName() + "=#{" + columnsList[i] + "}");
            // 最后一个字段不需要","
            if ((i + 1) < columnsList.length) {
                sb.append(",");
            }
        }
        String update="";
        if(isUUID){
        	update = "update " + tableName + " set " + sb.toString()
                    + " where UUID=#{uuid,jdbcType=VARCHAR}";
        }
        else update = "update " + tableName + " set " + sb.toString()
                + " where " + columnList.get(0).getColumnName() + "=#{" + idStr + "}";
        return update;
    }

    public String getUpdateSelectiveSql(String tableName,
            List<ColumnData> columnList) throws SQLException {
        StringBuffer sb = new StringBuffer();
        ColumnData cd = columnList.get(0); // 获取第一条记录，主键
        sb.append("\t<trim  suffixOverrides=\",\" >\n");
        for (int i = 1; i < columnList.size(); i++) {
            ColumnData data = columnList.get(i);
            String columnName = data.getColumnName();
            String jdbcType = data.getJdbcType();
            sb.append("\t<if test=\"").append(data.getAttrName())
                    .append(" != null ");
            // String 还要判断是否为空
            if ("String" == data.getDataType()) {
                sb.append(" and ").append(data.getAttrName()).append(" != ''");
            }
            sb.append(" \">\n\t\t");
            sb.append(columnName + "=#{" + data.getAttrName() + ",jdbcType="
                    + jdbcType + "},\n");
            sb.append("\t</if>\n");
        }
        sb.append("\t</trim>");
        String update="";
        if(isUUID){
        	update = "update " + tableName + " set \n" + sb.toString()
                    + " where UUID=#{uuid,jdbcType=VARCHAR}";
        }
        else update = "update " + tableName + " set \n" + sb.toString()
                + " where " + cd.getColumnName() + "=#{" + cd.getAttrName()
                + ",jdbcType=" + cd.getJdbcType() + "}";
        return update;
    }

    /**
     * <br>
     * <b>功能：</b>获取所有列名字<br>
     * <b>作者：</b>肖财高<br>
     * <b>日期：</b> 2011-12-21 <br>
     * 
     * @param tableName
     * @return
     * @throws SQLException
     */
    public String getColumnSplit(List<ColumnData> columnList)
            throws SQLException {
        StringBuffer commonColumns = new StringBuffer();
        for (ColumnData data : columnList) {
            commonColumns.append(data.getColumnName() + "|");
        }
        return commonColumns.delete(commonColumns.length() - 1,
                commonColumns.length()).toString();
    }

    public String getColumnAndJdbcTypeSplit(List<ColumnData> columnList)
            throws SQLException {
        StringBuffer commonColumns = new StringBuffer();
        for (ColumnData data : columnList) {
            commonColumns.append(data.getAttrName() + ",jdbcType="
                    + data.getJdbcType() + "|");
        }
        return commonColumns.delete(commonColumns.length() - 1,
                commonColumns.length()).toString();
    }

}
