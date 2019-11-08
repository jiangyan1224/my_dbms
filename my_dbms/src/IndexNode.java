import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Java中的集合有两类，一类是List，再有一类是Set。前者集合内的元素是有序的，元素可以重复；后者元素无序，但元素不可重复。
 * equals方法可用于保证元素不重复，
 * 但如果每增加一个元素就检查一次，若集合中现在已经有1000个元素，那么第1001个元素加入集合时，就要调用1000次equals方法。
 * 这显然会大大降低效率。 于是，Java采用了哈希表的原理。
 * 剩下的见 有关hashCode的作用：https://blog.csdn.net/a745233700/article/details/83186808
 */
public class IndexNode implements Serializable {

    //List有序可重复
    //Index有filePath和lineNum两个属性
    private List<Index> indexList;
    public IndexNode(){
        this.indexList=new ArrayList<>();
    }
    public void addIndex(Index index){
        indexList.add(index);
    }
    public Iterator<Index> indexIterator(){
        return indexList.iterator();
    }
    //获取到该IndexNode所含的所有Index所指的file
    public Set<File> getFiles(){
        //set无序不可重复 https://www.imooc.com/wenda/detail/340059
        //hashset是set的实现类，set是接口，set下面最主要的实现类就是hashset
        //要求添加到set里的类，一定要实现hashCode和equals方法，以保证元素的不可重复性
        Set<File> fileSet=new HashSet<>();
        Iterator<Index> indexIterator=indexIterator();
        for (Index index:indexList){
            File file=new File(index.getFilePath());
            fileSet.add(file);
        }
        return fileSet;
    }


}
