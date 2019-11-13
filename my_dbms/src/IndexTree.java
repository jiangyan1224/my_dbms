import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public class IndexTree implements Serializable {
    /**TreeMap一个很大的特点就是会对Key进行排序，使用了TreeMap存储键值对，再使用iterator进行输出时，会发现其默认采用key由小到大的顺序输出键值对
    通过红黑树(平衡二叉树)实现
     */
    private TreeMap<IndexKey,IndexNode> treeMap;
    public IndexTree(){
        treeMap=new TreeMap<>();
    }
    public TreeMap<IndexKey,IndexNode> getTreeMap(){
        return treeMap;
    }
    /**
     * IndexKey: value type
     * IndexNode: 里面有个indexList=ArrayList<Index>
     * Index: filePath lineNum
     */
    public void setTreeMap(TreeMap<IndexKey,IndexNode> treeMap){
        this.treeMap=treeMap;
    }
    public List<IndexNode> find(Relationship relationship,IndexKey condition){
        //////////////////////////////////////////////////2019.11.13
    }
}
