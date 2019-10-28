import java.io.*;
import java.util.*;

public class Table {
    private String name;//表名
    private File folder;//表所在的文件夹

    private static String userName;//用户姓名，切换或修改用户时修改
    private static String dbName;//数据库dataBase名，切换时修改

    private File dictFile;//数据字典
    private Map<String,Field> fieldMap;//字段映射集

    private LinkedHashSet<File> dataFileSet;//数据

    private File indexFile;//索引文件
    private Map<String,IndexTree> indexMap;//存放所有字段的索引树


    //控制文件行数
    private static long lineNumConfine=10;


    /*
    * 私有化构造函数
    * 防止table类在外部被实例化
    * 只能静态创建，所以构造函数私有??
    * */
    private Table(String name){
        this.name=name;
        this.fieldMap=new LinkedHashMap();
        this.folder=new File("dir"+"/"+userName+"/"+dbName+"/"+name);
        //数据字典
        this.dictFile=new File(folder,name+".dict");
        //数据
        this.dataFileSet=new LinkedHashSet<>();
        //索引文件
        this.indexFile=new File(folder,name+".index");
        this.indexMap=new HashMap<>();
    }

    /**
     * 初始化表信息，包括用户名和数据库名
     *
     * @param userName
     * @param dbName
     *
     */
    public static void init(String userName,String dbName){
        Table.userName=userName;
        Table.dbName=dbName;
    }

    /**
     * 创建一个新的表文件
     *
     * @param name 表名
     * @param fields
     * @return 如果表已存在，返回失败信息，否则返回success
     * 创建一个表，只是创建表的结构/属性，也就是有哪些field，data是要等之后再添加的
     *创建field时,是直接从给的参数Map<String,Field> fields中创建的，写入this.filedMap，
     * 同时要写入该表的dictFile中
     */
    public  static String createTable(String name,Map<String,Field> fields){
        if (existTable(name)) {
            return "创建表失败，因为已经存在表："+name;
        }

        Table table=new Table(name);

        table.dictFile.getParentFile().mkdirs();//创建真实目录，也就是该表的目录，下面存放data dict index文件
        table.addDict(fields);//通过table对象调用普通成员方法 PS：静态方法是不能直接调用非静态成员的
        return "success";
    }

    /**
     * 判断表是否存在
     * @param name
     * @return 存在与否
     */
    private static boolean existTable(String name){
        File folder=new File("dir"+"/"+userName+"/"+dbName+"/"+name);
        return folder.exists();
    }

    /**
     * 在字典文件中写入创建的字段信息，再将新增的字段map追加到this.filedMap字段映射集
     *
     * @param fields 字段列表
     * @return
     */
    public String addDict(Map<String,Field> fields){
        Set<String> keys=fields.keySet();
        for (String key:keys) {
            //如果当前table的fieldMap中已经有key，报错，避免出现相同field（Map不会有相同key的两个元素）
            if (fieldMap.containsKey(key))
                return "错误：存在重复添加的字段"+key;
        }
        writeDict(fields,true);//在字典文件中写入创建的字段信息

        fieldMap.putAll(fields);//把fields中所有映射复制到fieldMap中，键相同的fieldMap被覆盖
        return "success";
    }

    /**
     * 提供一组fields字段映射集，写入字典文件
     *
     * @param fields 字段映射集
     * @param append 是否在文件末尾追加
     */
    private void writeDict(Map<String,Field> fields,boolean append){
        try (
                //PrintWriter的构造函数接受FileWriter作为参数。得到PrintWriter实例之后调用其println()方法即可写入字符串
                //创建字符流
                FileWriter fw=new FileWriter(dictFile,true);//往dictFile中写入数据，以追加方式
                //字符打印流
                PrintWriter pw=new PrintWriter(fw);
                ) {
            for (Field field:fields.values()){
                String name=field.getName();
                String type=field.getType();
                //如果是主键，id string *
                if (field.isPrimaryKey()){
                    pw.println(name+" "+type+" "+"*");
                }else {//如果不是主键 id string ^
                    pw.println(name+" "+type+" "+"^");
                }
            }//end of for
        } catch (IOException e){//FileWriter，包含了FileNotFoundException
            e.printStackTrace();
        }
    }

    /**
     * 根据表名获取表
     * @param name 表名
     * @return 返回null或者Table对象
     */
    public static Table getTable(String name){
        if (!existTable(name)) return null;

        Table table=new Table(name);
        //start of 写入fields
        try(
                //字符读入流
                FileReader fr=new FileReader(table.dictFile);
                BufferedReader br=new BufferedReader(fr);//提供缓冲，用以加速
                ) {
            String line=null;
            //文件读到末尾是null
            while (null!=(line=br.readLine())){
                String[] fieldValues=line.split(" ");//用空格隔开
                Field field=new Field();
                field.setName(fieldValues[0]);
                field.setName(fieldValues[1]);
                //==表示两个变量指向同一个对象；覆盖/重写/override后的equals判断两个对象内容是否一致
                if ("*".equals(fieldValues[2])) field.setPrimaryKey(true);
                else field.setPrimaryKey(false);

                //把field的name作为fieldMap的key
                table.fieldMap.put(fieldValues[0],field);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }//end of 写入fields
        //start of写入data
        //listFiles()方法是返回某个目录下所有文件和目录的绝对路径，返回的是File数组，一个table可能有多个数据表
        File[] dataFiles=new File(table.folder,"data").listFiles();
        //目录为空，则该数组将为空。 如果此抽象路径名不表示目录，或返回I / O错误，则返回null 。
        if (null!=dataFiles&&0!=dataFiles.length){//确保new File的确存在
            for (int i=1;i<=dataFiles.length;i++){
                File dataFile=new File(table.folder+"/data",i+".data");
                table.dataFileSet.add(dataFile);
            }
        }//end of 写入data

        //写入索引文件
        if (table.indexFile.exists()) table.readInex();
        return table;

    }

    public Map<String,Field> getFieldMap() {return fieldMap;}


}
