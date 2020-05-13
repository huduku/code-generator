package cn.ucfgroup.createcode;

/**
 * 表字段类
 * 
 * @author Administrator
 */
public class ColumnData {

    private String attrName;
    private String firstUpperAttrName;//首字母大写，原因：由于velocity没有大小写转换的函数，所以只能后台组织
    private String columnName;
    private String dataType;
    private String columnComment;
    private String jdbcType;

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getFirstUpperAttrName() {
		return firstUpperAttrName;
	}

	public void setFirstUpperAttrName(String firstUpperAttrName) {
		this.firstUpperAttrName = firstUpperAttrName;
	}

	private Integer scale;

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
}
