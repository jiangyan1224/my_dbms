import java.io.Serializable;

public class IndexKey implements Comparable, Serializable {
    private String value;
    private String type;

    public IndexKey(String value,String type){
        this.value=value;
        this.type=type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    //因为实现了Comparable接口，IndexKey又是普通类不是抽象类，所以要实现接口的所有方法，也就compareTo这一个
    //比较两个IndexKey的value大小，分类别分别调用integer double string的compareTo方法来比较大小
    //只比较value大小，但是实际上有可能出现类型不同但是值相同的情况，比如123和"123"
    @Override
    public int compareTo(Object otherValue) {
        String keyValue=((IndexKey)otherValue).getValue();
        try {
            switch (type) {
                case "int":
                    return Integer.valueOf(value).compareTo(Integer.valueOf(keyValue));
                case "double":
                    return Double.valueOf(value).compareTo(Double.valueOf(keyValue));
                case "varchar":
                    return value.compareTo(String.valueOf(keyValue));
                default:
                    throw new Exception("条件限定不匹配");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //覆盖重写Object的equals方法
    //判断两个对象是否相等(内容相等)
    //这个函数和上面的compareTo不同，这个方法是可以判读拿出类型不同的，integer/String/double的equals方法都会判断类型
    @Override
    public boolean equals(Object o){
        if (this==o) return true;

        //Object的getClass方法返回该对象的运行时类，运行时类是Java反射机制的源头
        //getClass方法返回对象的类型，而面向对象语言的多态特性决定对象的类型只能在运行期确定（不同于声明的引用类型）
        if (o==null||getClass()!=o.getClass()) return false;

        IndexKey indexKey=(IndexKey) o;
        return value!=null?value.equals(indexKey.getValue()):indexKey.value==null;
        //因为不同type的值一定返回false 相同type的肯定会返回value相比的结果，所以type可以不比较
        //String的equals方法是挨个比较字符串的字母，有不一致就false
    }

    @Override
    //关于hashCode :HashCode经常用于确定对象的存储地址
    //如果两个对象相同， equals方法一定返回true，并且这两个对象的HashCode一定相同；
    //两个对象的HashCode相同，并不一定表示两个对象就相同，即equals()不一定为true，只能够说明这两个对象在一个散列存储结构中。
    //如果对象的equals方法被重写，那么对象的HashCode也尽量重写。
    //其他内容：hashCode作用：https://blog.csdn.net/a745233700/article/details/83186808
    public int hashCode(){
        //type可以推断出来，所以忽略以提升效率
        return value!=null?value.hashCode():0;

    }
}
