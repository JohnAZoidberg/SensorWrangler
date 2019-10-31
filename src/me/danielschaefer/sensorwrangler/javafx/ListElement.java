package me.danielschaefer.sensorwrangler.javafx;

public class ListElement<T> {
    private int index;
    private T value;
    public ListElement(int index, T value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() { return index; }
    public T getValue() { return value; }
}
