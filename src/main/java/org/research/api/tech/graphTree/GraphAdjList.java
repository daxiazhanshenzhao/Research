package org.research.api.tech.graphTree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

//TODO :等我考完就来重构数据结构，现在哭了你了
@Deprecated
public abstract class GraphAdjList<E> implements IGraph<E> {
    // 邻接表中表对应的链表的顶点
    private static class ENode {
        int adjvex; // 邻接顶点序号
        int weight;// 存储边或弧相关的信息，如权值
        ENode nextadj; // 下一个邻接表结点

        public ENode(int adjvex, int weight) {
            this.adjvex = adjvex;
            this.weight = weight;
        }
    }

    // 邻接表中表的顶点
    private static class VNode<E> {
        E data; // 顶点信息
        ENode firstadj; // //邻接表的第1个结点
    };

    private VNode<E>[] vexs;        // 顶点数组
    private int numOfVexs;          // 顶点的实际数量
    private int maxNumOfVexs;       // 顶点的最大数量
    private boolean[] visited;      // 判断顶点是否被访问过
    protected E current;            // 当前节点

    public GraphAdjList(int maxNumOfVexs) {
        this.maxNumOfVexs = maxNumOfVexs;
        vexs = (VNode<E>[]) Array.newInstance(VNode.class, maxNumOfVexs);
    }




    // 获取当前节点
    protected abstract E getFirstTech();

    // 设置当前节点
    public void setCurrent(E node) {
        this.current = node;
    }

    public E getCurrent() {
        if (current == null) {
            current = getFirstTech();
        }
        return current;
    }

    // 得到顶点的数目
    public int getNumOfVertex() {
        return numOfVexs;
    }

    // 插入顶点
    public boolean insertVex(E v) {
        if (numOfVexs >= maxNumOfVexs)
            return false;
        VNode<E> vex = new VNode<E>();
        vex.data = v;
        vexs[numOfVexs++] = vex;
        return true;
    }

    // 删除顶点
    public boolean deleteVex(E v) {
        for (int i = 0; i < numOfVexs; i++) {
            if (vexs[i].data.equals(v)) {
                for (int j = i; j < numOfVexs - 1; j++) {
                    vexs[j] = vexs[j + 1];
                }
                vexs[numOfVexs - 1] = null;
                numOfVexs--;
                ENode current;
                ENode previous;
                for (int j = 0; j < numOfVexs; j++) {
                    if (vexs[j].firstadj == null)
                        continue;
                    if (vexs[j].firstadj.adjvex == i) {
                        vexs[j].firstadj = null;
                        continue;
                    }
                    current = vexs[j].firstadj;
                    while (current != null) {
                        previous = current;
                        current = current.nextadj;
                        if (current != null && current.adjvex == i) {
                            previous.nextadj = current.nextadj;
                            break;
                        }
                    }
                }
                for (int j = 0; j < numOfVexs; j++) {
                    current = vexs[j].firstadj;
                    while (current != null) {
                        if (current.adjvex > i)
                            current.adjvex--;
                        current = current.nextadj;
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 定位顶点的位置
    public int indexOfVex(E v) {
        for (int i = 0; i < numOfVexs; i++) {
            if (vexs[i].data.equals(v)) {
                return i;
            }
        }
        return -1;
    }

    // 定位指定位置的顶点
    public E valueOfVex(int v) {
        if (v < 0 || v >= numOfVexs)
            return null;
        return vexs[v].data;
    }

    @Override
    public boolean insertEdge(int v1, int v2) {
        return insertEdge(v1,v2,1);
    }

    // 插入有向边（从v1到v2）
    public boolean insertEdge(int v1, int v2, int weight) {
        if (v1 < 0 || v2 < 0 || v1 >= numOfVexs || v2 >= numOfVexs)
            throw new ArrayIndexOutOfBoundsException();
        ENode vex1 = new ENode(v2, weight);

        // 索引为v1的顶点没有邻接顶点
        if (vexs[v1].firstadj == null) {
            vexs[v1].firstadj = vex1;
        }
        // 索引为v1的顶点有邻接顶点
        else {
            vex1.nextadj = vexs[v1].firstadj;
            vexs[v1].firstadj = vex1;
        }
        return true;
    }
    
    public boolean insertEdge(E v1, E v2) {
        if (v1 == null || v2 == null) {
            return false;
        } else {
            return insertEdge(indexOfVex(v1),indexOfVex(v2),1);
        }
    }

    // 删除有向边（从v1到v2）
    public boolean deleteEdge(int v1, int v2) {
        if (v1 < 0 || v2 < 0 || v1 >= numOfVexs || v2 >= numOfVexs)
            throw new ArrayIndexOutOfBoundsException();
        // 删除从v1到v2的边
        ENode current = vexs[v1].firstadj;
        ENode previous = null;
        while (current != null && current.adjvex != v2) {
            previous = current;
            current = current.nextadj;
        }
        if (current != null) {
            if (previous == null) {
                vexs[v1].firstadj = current.nextadj;
            } else {
                previous.nextadj = current.nextadj;
            }
            return true;
        }
        return false;
    }

    // 得到边
    public int getEdge(int v1, int v2) {
        if (v1 < 0 || v2 < 0 || v1 >= numOfVexs || v2 >= numOfVexs)
            throw new ArrayIndexOutOfBoundsException();
        ENode current = vexs[v1].firstadj;
        while (current != null) {
            if (current.adjvex == v2) {
                return current.weight;
            }
            current = current.nextadj;
        }
        return 0;
    }

    // 获取节点的所有后继节点（有向图中从当前节点出发能到达的节点）
    public List<Integer> getSuccessors(int v) {
        if (v < 0 || v >= numOfVexs)
            throw new ArrayIndexOutOfBoundsException();
        List<Integer> successors = new ArrayList<>();
        ENode current = vexs[v].firstadj;
        while (current != null) {
            successors.add(current.adjvex);
            current = current.nextadj;
        }
        return successors;
    }

    // 获取节点的所有前驱节点（有向图中能到达当前节点的节点）
    public List<Integer> getPredecessors(int v) {
        if (v < 0 || v >= numOfVexs)
            throw new ArrayIndexOutOfBoundsException();
        List<Integer> predecessors = new ArrayList<>();
        for (int i = 0; i < numOfVexs; i++) {
            if (i == v) continue;
            ENode current = vexs[i].firstadj;
            while (current != null) {
                if (current.adjvex == v) {
                    predecessors.add(i);
                    break;
                }
                current = current.nextadj;
            }
        }
        return predecessors;
    }

    // 前进到下一个节点（模板方法）
    public void nextNode() {
        if (current == null) {
            return;
        }
        
        // 获取当前节点的索引
        int currentIndex = indexOfVex(current);
        if (currentIndex == -1) {
            return;
        }
        
        // 获取当前节点的所有后继节点
        List<Integer> successors = getSuccessors(currentIndex);
        
        // 如果有后继节点，将第一个后继节点设置为当前节点
        if (!successors.isEmpty()) {
            int nextIndex = successors.get(0);
            E nextNode = valueOfVex(nextIndex);
            if (nextNode != null) {
                current = nextNode;
            }
        } else {
            // 如果没有后继节点，将current设为null表示到达终点
            current = null;
        }
    }
}
