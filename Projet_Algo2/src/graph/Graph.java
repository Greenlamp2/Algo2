package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class Graph {
    private Node firstNode;
    int cpt;
    int nbSommet;
    
    public Graph(){
        firstNode = null;
        cpt = 1;
        nbSommet = 0;
    }

    public void addNode(String id, String name, int value) {
        Node newNode = new Node();
        newNode.setName(name);
        newNode.setValue(value);
        newNode.setId(id);
        boolean found = false;
        if(this.getFirstNode() == null){
            this.setFirstNode(newNode);
        }else{
            Node node = this.getFirstNode();
            while(node.getNextNode() != null){
                if(node.getNextNode().getId().equals(newNode.getId())){
                    found = true;
                }
                node = node.getNextNode();
            }
            if(!found){
                newNode.setPrevNode(node);
                node.setNextNode(newNode);
            }
        }
    }

    public void addArc(String nameSrc, String nameDest, int value) throws Exception {
        Node node = this.getFirstNode();
        Node nodeSrc = null;
        Node nodeDest = null;
        while(node != null || (nodeSrc == null && nodeDest == null)){
            if(nodeSrc == null && node.getName().equals(nameSrc)){
                nodeSrc = node;
            }else if(node.getName().equals(nameDest)){
                nodeDest = node;
            }
            node = node.getNextNode();
        }
        if(nodeSrc != null && nodeDest != null){
            Arc newArc = new Arc();
            newArc.setOriginNode(nodeSrc);
            newArc.setDestNode(nodeDest);
            newArc.setLabel(String.valueOf(value));
            Arc arcSrc = nodeSrc.getArc();
            if(arcSrc == null){
                nodeSrc.setArc(newArc);
            }else{
                while(arcSrc.getNextArc() != null){
                    arcSrc = arcSrc.getNextArc();
                }
                arcSrc.setNextArc(newArc);
            }
        }else{
            if(nodeSrc == null && nodeDest == null){
                throw new Exception("Node source et node destination introuvable");
            }else if(nodeSrc == null){
                throw new Exception("Node source introuvable");
            }else if(nodeDest == null){
                throw new Exception("Node destination introuvable");
            }
        }
    }
    
    public void toTxt(String nameFile,String etape) {
        
        LinkedList<String> data = new LinkedList<String>();
        
        data.add("digraph G {");
        
        data.add("node [color=orange, style=filled]");
        Node node = this.getFirstNode();
        while(node != null){
            Arc arc = node.getArc();
            while(arc != null){
                data.add(arc.getOriginNode().getId() + " -> " + arc.getDestNode().getId() + "[label="+arc.getLabel()+"]");
                arc = arc.getNextArc();
            }
            node = node.getNextNode();
        }
        node = this.getFirstNode();
        while(node != null){
            data.add(node.getId() + " [label = \""+node.getName()+"\n"+node.getValue()+"\"]");
            node = node.getNextNode();
        }
        data.add("}");
        try{
            PrintWriter out  = new PrintWriter(new FileWriter(nameFile+etape+".gv"));
            for (String line : data){
                out.println(line);
            }
            out.close();
        }
        catch(Exception e){
            System.err.println("error: "+e.getMessage());
        }
    }

    public void profondeur(Node node) throws Exception {
        node.setMarque(true);
        Arc arc = node.getArc();
        System.out.println(node.getName());
        while(arc != null){
            if(arc.getDestNode() != null && !arc.getDestNode().isMarque()){
                profondeur(arc.getDestNode());
            }
            arc = arc.getNextArc();
        }
    }

    public Node getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(Node firstNode) {
        this.firstNode = firstNode;
    }

    public void unmark() {
        Node node = this.firstNode;
        while(node != null){
            node.setMarque(false);
            node = node.getNextNode();
        }
    }

    public boolean getCycles(Node node, Vec vec) throws Exception{
        vec.add(node.getName());
        Arc arc = node.getArc();
        while(arc != null){
            if(!vec.contains(arc.getDestNode().getName())){
                if(getCycles(arc.getDestNode(), vec)){
                    return true;
                }
            }else{
                if(arc.getDestNode().getName().equals(vec.getFirst())){
                    Graph cycle = buildCycle(vec);
                    simplify(cycle);
                    return true;
                }
            }
            arc = arc.getNextArc();
        }
        vec.remove(node.getName());
        return false;
    }
    
    private void simplify(Graph cycle) throws Exception{
        Node node = cycle.getFirstNode();
        int min = Integer.parseInt(node.getArc().getLabel());
        while(node != null && node.getArc() != null){
            if(Integer.parseInt(node.getArc().getLabel()) < min){
                min = Integer.parseInt(node.getArc().getLabel());
            }
            node = node.getArc().getDestNode();
        }
        
        System.out.println(cpt + ") reduction de " + min);
        cycle.showSituation();
        cpt++;
        
        node = cycle.getFirstNode();
        while(node != null && node.getArc() != null){
            Node temp = node.getArc().getDestNode();
            simplifyBy(cycle, node.getArc().getOriginNode().getName(), node.getArc().getDestNode().getName(), min);
            node = temp;   
        }
        cycle.removeArcZero();
        System.out.println("nouvelle situation :");
        cycle.showNouvelleSituation();
    }
    
    private void simplifyBy(Graph cycle, String src, String dst, int min) throws Exception{
        Node node = cycle.getFirstNode();
        while(node != null && node.getArc() != null){
            Node temp = node.getArc().getDestNode();
            if(node.getArc().getOriginNode().getName().equals(src) && node.getArc().getDestNode().getName().equals(dst)){
                int num = Integer.parseInt(node.getArc().getLabel()) - min;
                node.getArc().setLabel(String.valueOf(num));
            }
            node = temp;
        }
        
        unmark();
        
        unmark();
        simplifyMe(getNode(src), src, dst, min);
    }
    
    private boolean simplifyMe(Node node, String src, String dst, int min){
        node.setMarque(true);
        Arc arc = node.getArc();
        while(arc != null){
            if(arc.getOriginNode().getName().equals(src) && arc.getDestNode().getName().equals(dst)){
                int num = Integer.parseInt(arc.getLabel()) - min;
                if(num == 0){
                    node.setArc(node.getArc().getNextArc());
                }else{
                    arc.setLabel(String.valueOf(num));
                }
                return true;
            }
            if(!arc.getDestNode().isMarque()){
                if(simplifyMe(arc.getDestNode(), src, dst, min)){
                    return true;
                }
            }
            arc = arc.getNextArc();
        }
        return false;
    }  

    public Node getNode(String nameNode) throws Exception {
        Node node = this.firstNode;
        while(node != null && !node.getName().equals(nameNode)){
            node = node.getNextNode();
        }
        if(node == null){
            throw new Exception("Sommet "+nameNode+" introuvable");
        }
        return node;
    }

    private Graph buildCycle(Vec vec) throws Exception {
        Graph newGraph = new Graph();
        int nbElm = vec.getVec().size();
        for(int i=0; i<nbElm-1; i++){
            String nameNode = vec.getVec().get(i);
            String nameNextNode = vec.getVec().get(i+1);
            Arc arc = getArc(nameNode, nameNextNode);
            Node node = getNode(nameNode);
            Node nextNode = getNode(nameNextNode);
            newGraph.addNode(node.getId(), node.getName(), node.getValue());
            newGraph.addNode(nextNode.getId(), nextNode.getName(), nextNode.getValue());
            newGraph.addArc(nameNode, nameNextNode, Integer.parseInt(arc.getLabel()));
        }
        String nameNode = vec.getVec().get(vec.getVec().size()-1);
        String nameNextNode = vec.getVec().get(0);
        Arc arc = getArc(nameNode, nameNextNode);
        Node node = getNode(nameNode);
        Node nextNode = getNode(nameNextNode);
        newGraph.addNode(node.getId(), node.getName(), node.getValue());
        newGraph.addNode(nextNode.getId(), nextNode.getName(), nextNode.getValue());
        newGraph.addArc(nameNode, nameNextNode, Integer.parseInt(arc.getLabel()));
        return newGraph;
    }

    private Arc getArc(String nameNode, String nameNextNode) throws Exception {
        Node node = this.firstNode;
        while(node != null){
            Arc arc = node.getArc();
            while(arc != null){
                if(arc.getOriginNode().getName().equals(nameNode) && arc.getDestNode().getName().equals(nameNextNode)){
                    return arc;
                }
                arc = arc.getNextArc();
            }
            node = node.getNextNode();
        }
        throw new Exception("Arc "+nameNode+" - " +nameNextNode+ " introuvable");
    }

    private void showSituation() {
        Node node = firstNode;
        while(node != null && node.getArc() != null){
            System.out.print (node.getName() + " (" + node.getArc().getLabel() + ")-> ");
            node = node.getArc().getDestNode();
        }
        System.out.println(firstNode.getName() + " (" + firstNode.getArc().getLabel() + ")-> ...");
    }

    private void showNouvelleSituation() {
        boolean see = false;
        Node node = this.getFirstNode();
        String name = null;
        Arc arc = null;
        while(node != null){
            arc = node.getArc();
            if(arc != null){
                System.out.print(arc.getOriginNode().getName() + " (" + arc.getLabel() + ")-> ");
                name = arc.getDestNode().getName();
                see = true;
            }else{
                if(see){
                    System.out.println(node.getName());
                    break;
                }
            }
            node = node.getNextNode();
        }
        System.out.println("");
        System.out.println("");
    }

    private void removeArcZero() {
        Node node = this.getFirstNode();
        while(node != null){
            if(node.getArc() != null && node.getArc().getLabel().equals("0")){
                node.setArc(null);
            }
            node = node.getNextNode();
        }
    }
    
    public void rembourser() {
        Stack<Node> pile = new Stack<Node>();
        Node node = null;
        
        initRemboursement();

        node = this.firstNode;
        while(node != null){
            if(node.getMarqueTri()==-1)
                explore(node,pile);
            node = node.getNextNode();
        }
        
        rembourseGraph(pile);
    }

    private void explore(Node node, Stack<Node> pile) {
            node.setMarqueTri(0);
            Arc arc = node.getArc();
            while(arc != null){
                    if(arc.getDestNode().getMarqueTri()==-1)
                        explore(arc.getDestNode(),pile);
                arc = arc.getNextArc();
            }
            pile.push(node);
    }
    
    public void traceTri(Node node,Arc arc, int indic) {
        
        String add="";
        int rembourse=0;      
        if(indic ==1) {
            rembourse = Integer.parseInt(arc.getLabel()) - (Integer.parseInt(arc.getLabel()) - node.getValue());
            add = " (Il reste "+(Integer.parseInt(arc.getLabel())-node.getValue())+" à rembourser)";
        }
        else
            rembourse = Integer.parseInt(arc.getLabel());
            
        System.out.print(node.getName()+" ("+rembourse+")-> ");
        System.out.print(arc.getDestNode().getName());
        System.out.print(add);

        System.out.println("");
    }
    
    public void deleteArc(Node node, Arc arcToDelete) {
        
        Arc arc = node.getArc();
        Arc saveArc = node.getArc();
        int cpt = 0;
        while(arc!=null) {
            if(arc == arcToDelete) {
                if(cpt == 0)
                    node.setArc(arc.getNextArc());
                else
                    saveArc.setNextArc(arc.getNextArc());
            }
            arc = arc.getNextArc();
            if(cpt!=0)
                saveArc = saveArc.getNextArc();
            cpt++;
        }
    }

    private void initRemboursement() {
        Node node = this.firstNode;
        while(node != null){
            node.setMarqueTri(-1);
            node = node.getNextNode();
        }
    }

    private void rembourseGraph(Stack<Node> pile) {
        LinkedList<Arc> listArc = null;
        int valueOrigin=0,valueDest=0,valueRemb=0;
        Arc arc = null;
        Node destNode=null;
        
        for(int i=0; i<nbSommet; i++)
        {
            Node n = pile.pop();
            valueOrigin = n.getValue();

            listArc = new LinkedList<Arc>();
            arc = n.getArc();
            while(arc!=null)
            {
                listArc.add(arc);
                arc = arc.getNextArc();
            }
            Collections.sort(listArc);
            
            for(Arc arc2 : listArc)
            {
                destNode = arc2.getDestNode();
                valueDest = destNode.getValue();
                valueRemb = Integer.parseInt(arc2.getLabel());
                
                if(valueOrigin >= valueRemb) {
                    traceTri(n,arc2,0);
                    n.setValue((valueOrigin-valueRemb));
                    destNode.setValue(destNode.getValue()+valueRemb);
                    deleteArc(n,arc2);
                    valueOrigin=(valueOrigin-valueRemb);
                }
                else {
                    traceTri(n,arc2,1);
                    n.setValue(0);
                    destNode.setValue(destNode.getValue()+valueOrigin);
                    arc2.setLabel((valueRemb-valueOrigin)+"");
                    valueOrigin = 0;
                }
            }
        }
    }
}