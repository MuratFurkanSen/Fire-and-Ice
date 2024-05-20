public class Queue {
    private int rear,front;
    private Object[] elements;

    public Queue(int capacity){
        elements = new Object[capacity];
        rear = -1;
        front = 0;

    }
    void enqueue(Object data){
        if(!isFull()){
            rear++;
            elements[rear] = data;
        }

    }
    Object dequeue(){
        if(!isEmpty()){
            Object retdata = elements[front];
            elements[front] = null;
            for (int i = front; i <rear ; i++) {
                elements[i] = elements[i+1];
            }
            rear--;
            return retdata;
        }
        else return null;

    }

    Object peek(){
        if(!isEmpty()){
            return elements[front];
        }
        else return null;
    }
    boolean isEmpty(){
        return rear<front;
    }
    boolean isFull(){
        return rear+1== elements.length;
    }

    void reverse(){
        int size = size();
        Stack s = new Stack(size);
        for (int i = 0; i <size ; i++) {
            s.push(dequeue());
        }
        for (int i = 0; i < size; i++) {
            enqueue(s.pop());
        }


    }

    int size(){
        return rear-front+1;
    }



}
