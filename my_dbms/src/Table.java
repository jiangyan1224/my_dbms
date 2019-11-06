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
     * 删除某字段field
     * @param fieldName 字段名
     */
    public String deleteDict(String fieldName){
        if (!fieldMap.containsKey(fieldName)) return "错误：不存在字段："+fieldName;

        fieldMap.remove(fieldName);//从table对象中删除field

        writeDict(fieldMap,false);//向dictFile中写入remove后的fieldMap，false清空原有内容再写入
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
            /**
             * 1.对于流的关闭顺序，一种是按照后创建流先关闭的原则
             * 另一种直接关闭包装流即可，例如：
             *BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("a.txt")));
             *这里处理你的业务逻辑
             *reader.close();//在这里关闭就把所有流都给关掉了，不需要再额外去关闭了
             * pw.close();
             *
             * 2.try后面的()是1.7的新特性，括号里的内容支持包括流以及任何可关闭的资源，数据流会在 try 执行完毕后自动被关闭
             */
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

        //写入索引文件到table对象
        if (table.indexFile.exists()) table.readIndex();
        return table;

    }

    public Map<String,Field> getFieldMap() {return fieldMap;}

    /**
     * 从索引文件读取索引对象
     */
    private void readIndex(){
        if (!indexFile.exists()) return;
        try (
                //从文件中读出一个对象到内存，可以用ObjectInputStream套接FileInputStream来实现（序列化）
                FileInputStream fis=new FileInputStream(indexFile);
                ObjectInputStream ois=new ObjectInputStream(fis);
                ){
            indexMap=(Map<String, IndexTree>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 删除表
     */
    public static String dropTable(String name){
        if (!existTable(name)) return "错误：不存在表："+name;
        File folder =new File("dir"+"/"+userName+"/"+dbName+"/"+name);
        deleteFolder(folder);
        return "success";
    }
    /**
     * 删除文件/文件夹
     */
    //静态方法是不能直接调用非静态成员的
    private static void deleteFolder(File file){
        if (file.isFile()) file.delete();//删除文件
        else if (file.isDirectory()){//文件夹,则要删除目录下所有文件和当前文件夹
            File[] files=file.listFiles();//返回所有文件
            for (int i=1;i<=files.length;i++){
                deleteFolder(files[i]);//删除文件
            }
            file.delete();//删除文件夹
        }
    }

    /**
     * 对空位填充 fillStr 填充后的字段按数据字段顺序排序(不为主键的data，值可以为空)
     * @param fillStr 要填充的字符串
     * @param data 原始数据
     * @return  填充后的数据
     */
    private Map<String,String> fillData(Map<String,String>data,String fillStr){
        /**
         * 输入的data应该是Map形式，
         * id 123
         * sex 0
         * height 1.8
         * 这样
         */
        //fillData是真正写入文件的集合，空位补fillStr
        Map<String,String> fillData=new LinkedHashMap<>();
        //遍历数据字典
        for (Map.Entry<String,Field> fieldEntry: fieldMap.entrySet()){
            String fieldKey=fieldEntry.getKey();//返回每个field的name，比如id
            if (data.get(fieldKey)==null){//data中id对应的输入值是null
                data.put(fieldKey,fillStr);
            }else {//对应输入值不为null，原样写到fillData
                fillData.put(fieldKey,data.get(fieldKey));
            }
        }
        return fillData;
    }

    /**
     *利用正则表达式判断data类型是否与数据字典相符
     * @param data
     * @return
     */
    private boolean checkType(Map<String,String> data){
        if (data.size()!=fieldMap.size()) return false;

        //遍历data.value和field.type，逐个对比类型
        Iterator<Field> fieldIter=fieldMap.values().iterator();
        while (fieldIter.hasNext()){
            Field field =fieldIter.next();//返回field 如 id int *
            String dataValue=data.get(field.getName());// data: id 123

            //如果是[NULL]则跳过类型检查
            if ("[NULL]".equals(dataValue)) continue;
            switch (field.getType()){
                case "int":
                    //正则表达式：\\+转义为加号，-因为在正则中没有专门的意思，所以不用转义
                    // |指或  ?指前面的东西出现零次或一次  \\d转义为整数  +前面的东西至少出现一次
                    //^以此开始，不包括^  $以此结束，不包括$
                    if (!dataValue.matches("^(-|\\+)?\\d+$"))
                        return false;
                    break;
                case "double":
                    //*前面的东西出现零次或多次
                    if (!dataValue.matches("^([-+])?\\d*\\.?\\d+$"))  //源代码
//                      if (!dataValue.matches("^(-|\\+)?[0-9]+\\.?[0-9]*"))不匹配“ .123 ”的情况
                        return false;
                    break;
                case "varchar":
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * 插入数据到最后一个数据文件
     * 如果数据行数超过限定值，写入到下一个文件中
     * @param srcData
     * @return
     */
    public String insert(Map<String,String> srcData){
        File lastFile=null;
        int lineNum=0;
        int fileNum=0;
//        dataFileSet：LinkedHashSet<File> dataFileSet
        for (File file:dataFileSet){
            fileNum++;
            lastFile=file;
            //lineNum记录最后一个文件的文件行数
            lineNum=fileLineNum(lastFile);
        }
        //如果没有数据文件，新建一个1.data
        //table初始化的时候dataFileSet就已经给了一个空的LinkedHashSet<File>
        //getTable里面，获取对应的data文件，用的listFiles()，
        // 判断返回结果为null或者返回结果的长度为0（不是一个目录或者目录下没有东西）
        if (null==lastFile||0==fileNum){
            //folder指当前表的目录，如table1目录
            lastFile=new File(folder+"/data",1+".data");
            dataFileSet.add(lastFile);
            lineNum=0;
        }else if (lineNumConfine<=fileLineNum(lastFile)){
            //如果最后一个文件行数大于行数限制，新建数据文件
            lastFile=new File(folder+"/data",fileNum+1+".data");
            dataFileSet.add(lastFile);
            lineNum=0;
        }

        //添加索引
        for (Map.Entry<String,Field> fieldEntry:fieldMap.entrySet()){
            String dataName=fieldEntry.getKey();
            String dataValue=srcData.get(dataName);
            //如果发现此数据为空，不添加到索引树中
            if (null==dataValue|| "[NULL]".equals(dataValue)){
                continue;
            }
            String dataType=fieldEntry.getValue().getType();


        }
    }




}
