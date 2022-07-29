package heap.binary;

import heap.Element;
import heap.EmptyHeapException;
import heap.Heap;

import java.util.ArrayList;
import java.util.Collections;

import static heap.handig.VGL.*;

/**
 * Een binaire hoop geïmplementeerd als impliciete datastructuur.
 * Heeft een parameter root die aangeeft waar in de lijst de hoop start.  Maar deze heb ik verder nooit gebruikt.
 */
public class BinaryHeap<T extends Comparable<T>> implements Heap<T> {
    private int root = 0;
    private ArrayList<Node<T>> lijst;

    public BinaryHeap() {
        lijst = new ArrayList<>();
    }

    /**
     * Voegt een element toe door het op het einde van de lijst te plaatsen en dan opwaarts te percoleren.
     * @param value De waarde voor het nieuwe element.
     * @return Een object dat toegang verleent tot het element.
     */
    @Override
    public Element<T> insert(T value) {
        Node<T> node = new Node<T>(value,lijst.size());
        lijst.add(node);
        fixUp(lijst.size()-1);
        return node;
    }

    @Override
    public Element<T> findMin() throws EmptyHeapException {
        checkEmpty();
        return lijst.get(root);
    }

    /**
     * Verwijdert de root door hem te verwisselen met het laatste element.  Daarna wordt dit element naar beneden gepercoleerd.
     * @return De waarde van de root.
     * @throws EmptyHeapException als de hoop leeg is.
     */
    @Override
    public T removeMin() throws EmptyHeapException {
        checkEmpty();
        T value = value(root);
        swap(root,lijst.size()-1);
        lijst.get(lijst.size()-1).position = -1;
        lijst.remove(lijst.size()-1);
        fixDown(root);
        return value;
    }

    @Override
    public boolean isEmpty() {
        return root>=lijst.size();
    }

    /**
     * Opwaartse percolatie van een element.
     * @param i de startindex
     */
    private void fixUp(int i){
        if(i<=root){
            return;
        }
        int p = parent(i);
        while(i> root && isKl(lijst.get(i).value(),lijst.get(p).value())){
            swap(i,p);
            i = p;
            p = parent(i);
        }
    }

    private void swap(int i, int j){
        lijst.get(i).position = j;
        lijst.get(j).position = i;
        Collections.swap(lijst,i,j);
    }

    private int parent(int i){
        if(i==root){
            return i;
        }
        int i_corr = i-root+1;
        int p_corr = i_corr/2;
        return p_corr-1+root;
    }


    private int firstChild(int i){
        int i_corr = i-root+1;
        return 2*i_corr-1;
    }

    public int smallestChild(int i){
        if(firstChild(i)+1>=lijst.size()){return firstChild(i);}
        if(isKl(value(firstChild(i)),value(firstChild(i)+1))){
            return firstChild(i);
        }else {
            return firstChild(i)+1;
        }
    }

    private boolean hasChild(int i){
        return firstChild(i)<lijst.size();
    }

    /**
     * Neerwaartse percolatie.
     * @param i startindex
     */
    private void fixDown(int i) {
        while(hasChild(i)){
            int smallestChild = smallestChild(i);
            if(isKl(value(smallestChild),value(i))){
                swap(i,smallestChild);
                i = smallestChild;
            }else{
                return;
            }
        }
    }

    /**
     * Percoleert een element naar boven of beneden al naar gelang dit nodig blijkt.
     * @param i
     */
    public void fix(int i){
        if(i<root || i>=lijst.size()){
            return;
        }

        if(i==root){
            fixDown(i);
        }else if(isKl(value(i),value(parent(i)))){
            fixUp(i);
        }else{
            fixDown(i);
        }
    }

    private T value(int index){
        return lijst.get(index).value();
    }


    public void checkEmpty() throws EmptyHeapException {
        if (root>=lijst.size()) {
            throw new EmptyHeapException();
        }
    }

    /**
     * Een controlemethode.
     * @return of elke node zijn juiste index kent.
     */
    public boolean checkIndices(){
        boolean a = true;
        for(int i = 0; i < lijst.size(); i++){
            a = a && (lijst.get(i).position==i);
        }
        return a;
    }


    @Override
    public String toString(){
        return lijst.toString();
    }



    public class Node<T extends Comparable<T>> implements Element<T> {
        private T value;
        private int position;

        public Node(T value, int pos) {
            this.value = value;
            position = pos;
        }


        @Override
        public T value() {
            return value;
        }

        /**
         * Verwijdert dit element door het te vervangen door het laatste element in de lijst.
         */
        @Override
        public void remove() {
            if(removed()){
                return;
            }

            int k = position;
            swap(position,lijst.size()-1);
            lijst.remove(lijst.size()-1);
            position = -1;
            fix(k);
        }


        /**
         * Update aan de hand van percolatie.
         * @param value nieuwe waarde
         */
        @Override
        public void update(T value) {
            if(removed()){
                return;
            }
            this.value = value;
            if(isKl(value,BinaryHeap.this.value(parent(position)))){
                fixUp(position);
            }else{
                fixDown(position);
            }
        }

        public boolean removed(){
            return position==-1;
        }

        @Override
        public String toString(){
            return value.toString();
        }
    }
}