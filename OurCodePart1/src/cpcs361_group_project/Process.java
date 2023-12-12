
package cpcs361_group_project;

public class Process {

    private String name;
    private int size;
    private int base;

    public Process(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int baseStart) {
        this.base = baseStart;
    }

    @Override
    public String toString() {
        return String.format("address[%d:%d] Process %s", getBase(), getSize() + getBase() - 1, getName());
    }

}
