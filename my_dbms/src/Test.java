public class Test {
    public static void main(String[] args){
//        System.out.println(Integer.valueOf("123")); 123
//        System.out.println(Integer.valueOf(123)); 123
//        System.out.println(Integer.valueOf("123").compareTo(Integer.valueOf(123))); 返回0 相等
//        System.out.println("123".equals(123)); false
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;

//        System.out.println(c == d); //true
//        System.out.println(e == f);  //false
//        System.out.println(c == (a + b));  //true
//        System.out.println(c.equals(a + b));  //true

        /**
         * 自动提升：多种不同数据类型的表达式中，类型会自动向范围比较大的值的数据类型提升。
         * 右边的(a+b)在拆箱相加再装箱之后，因为左右类型不同，发生自动提升，int提升到long
         * 如果直接g==d会报错，提示两边类型不同
         *
         * 因为这里有运算，所以就是比较值，又因为类型自动提升，所以==两边类型就相同了，返回true
         * equals是因为不是表达式，没有类型自动提升，所以两边类型不同，返回false
         */
        System.out.println(g == (a+b));  //true
        System.out.println(g.equals(a + b));  //false

    }
}
