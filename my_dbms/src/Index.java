import java.io.Serializable;
//实现序列化接口，可以把对象写入到磁盘中
//序列化：Java对象可以通过对象字节流持久化到文件中
//反序列化：将持久化写入文件里的对象数据转化为Java对象
public class Index implements Serializable{
    private String filePath;
    private int lineNum;

    public Index(String filePath,int lineNum){
        this.filePath=filePath;
        this.lineNum=lineNum;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNum() {
        return lineNum;
    }
}
