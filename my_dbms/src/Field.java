
//描述列
public class Field {
    //name type size primaryKey isnull
    private String name;//列名
    private String type;//数据类型
    private boolean primaryKey;//是否作为主键

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
