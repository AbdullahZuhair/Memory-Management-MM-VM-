
package cpcs361_group_project;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Memory {

    private int maxSize, used = 0, pointer=0;
    private ArrayList<Process> list = new ArrayList<>();
    private Scanner input = new Scanner(System.in);
    
    public Memory() throws Exception {
        init();
        menu();
    }

    private void init() throws Exception {
        do {
            try {
                System.out.print("Enter a size of memory in Megabyte: ");
                setMaxSize(((input.nextInt() * 1048576)-1));
                if (getMaxSize() <= 0) {
                    throw new IllegalArgumentException();
                }
            } catch (InputMismatchException error) {
                System.err.println("Error: Your input is not an integer.");
                input.next();
                continue;
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Enter a positive integer > 0");
                continue;
            } catch (Exception e) {
                System.err.println("Error: Unknown");
                continue;
            }
        } while (getMaxSize() <= 0);
        System.out.printf("\nYou have entered %d megabyte.\n",getMaxSize() / 1048575);
    }

    private void menu() throws Exception {
        int current = 0;
        do {
            try {
                System.out.printf("\nMemory size is: %,d bytes"
                + "\nMemory used is: %,d bytes"
                + "\nMemory free is: %,d bytes\n",
                (getMaxSize()), getUsed(), getFree());
                
                System.out.print("\nSelect one of the commands:\n"
                        + "1) Request\n"
                        + "2) Release\n"
                        + "3) Compact\n"
                        + "4) Stat\n\n"
                        + "5) Exit\n"
                        + "-----------------------\n"
                        + "command number: ");
                current = input.nextInt();
                if (current > 5 || current < 1) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: input is wrong try between 1 and 5.");
                continue;
            } catch (InputMismatchException e2) {
                System.err.println("Error: Your input is not an integer.");
                input.next();
                continue;
            } catch (Exception e) {
                System.err.println("Error: Unknown");
                continue;
            }
            switch (current) {
                case 1:
                    Request();
                    break;
                case 2:
                    Release();
                    break;
                case 3:
                    Compact();
                    break;
                case 4:
                    Stat();
                    break;
                case 5:
                    System.out.println("Thank you!");
                    System.exit(0);
            }
        } while (current != 5);
    }

    private void Request() throws InputMismatchException, IllegalArgumentException{
        if(getMaxSize()<= getUsed() && !emptyExist()){
            System.err.printf("out of memory:\n"
                    + "Used: %,d\n"
                    + "Max: %,d\n",getUsed(),getMaxSize());
        }else{
            String requestArg1, requestArg3 = null;
            int requestArg2 = 0;
            System.out.print("Enter the process name, size, and allocation flag(i.e. P0 40000 W): ");
            requestArg1 = input.next();
            try{
                requestArg2 = input.nextInt();
                if(requestArg2<=0){
                    throw new IllegalArgumentException();
                }
            }catch(InputMismatchException e){
                System.err.println("Error arg2: Your input is not an integer.");
                input.next();
                return;
            }catch(IllegalArgumentException e){
                System.err.println("Error arg2: Your input is <= 0.");
                return;
            }
            if(requestArg2>getMaxSize()){
                System.err.println("The process size is > memory size.");
                return;
            }
            requestArg3 = input.next();
            
            Process process = new Process(requestArg1, requestArg2);
            switch (requestArg3.toLowerCase()){
                case "b":
                    BestFit(process);
                    break;
                case "f":
                    FirstFit(process);
                    break;
                case "w":
                    WorstFit(process);
                    break;
                default:
                    System.err.println("Wrong fit flag, try again.");
                    break;
                }
            }
        }
    
    private void Release() {
        System.out.print("Enter the process name you want to release: ");
        String name = input.next();
        for (int i = 0; i < getList().size(); i++) {
            if(getList().get(i).getName().equalsIgnoreCase(name)){
                System.out.println("Process "+getList().get(i).getName()+" has been released from memory.");
                getList().get(i).setName("Empty");
            }
        }
    }

    private void Compact() {
        int sum = 0;
        for (int i = 0; i < getList().size(); i++) {
            if(getList().get(i).getName().equalsIgnoreCase("Empty")){
                for (int j = i+1; j < getList().size();) {
                    if(getList().get(j).getName().equalsIgnoreCase("Empty")){
                        sum = getList().get(i).getSize()+getList().get(j).getSize();
                        getList().get(i).setSize(sum);
                        getList().remove(getList().get(j));
                    }else{
                        break;
                    }
                }
            }
        }
        System.out.println("Compacted "+sum+" bytes");
    }

    private void Stat() {
        for (int i = 0; i < getList().size(); i++) {
            System.out.println(getList().get(i).toString());
        }
    }
    
    
    private void BestFit(Process process) {
        int index = minIndex(process.getSize());
        if(index == -1 && process.getSize() <= getFree()){
            getList().add(process);
            process.setBase(getPointer());
            setPointer(getPointer()+process.getSize());
            setUsed(getUsed()+process.getSize());
            System.out.println("Done!");
        }else if(process.getSize()< getList().get(index).getSize()){
                getList().get(index).setName(process.getName());
                if(process.getSize()< getList().get(index).getSize()){
                    Process temp = new Process("Empty", getList().get(index).getSize() - process.getSize());
                    getList().add(index+1,temp);
                    getList().get(index+1).setBase(getList().get(index).getBase() + getList().get(index).getSize());
                }
                getList().get(index).setSize(process.getSize());
                System.out.println("Done!");
                }else{
                    System.err.println("No memory available.");
                }
    }

    private void FirstFit(Process process) {
        Process temp;
        if(process.getSize()<=(getMaxSize()-getUsed())&& getList().isEmpty()){
            getList().add(process);
            process.setBase(getPointer());
            setPointer(getPointer()+process.getSize());
            setUsed(getUsed()+process.getSize());
            System.out.println("Done!");
        }else{
            for (int i = 0; i < getList().size(); i++) {
                temp = getList().get(i);
                if(temp.getName().equalsIgnoreCase("Empty") && temp.getSize()>=process.getSize()){
                    temp.setName(process.getName());
                    if(process.getSize()< temp.getSize()){
                        getList().add(getList().indexOf(temp) + 1, new Process("Empty", temp.getSize()-process.getSize()));
                        getList().get(getList().indexOf(temp) + 1).setBase(process.getBase()+temp.getBase());
                    }
                    temp.setSize(process.getSize());
                    System.out.println("Done!");
                }
            }
        }
    }

    private void WorstFit(Process process) {
        int index = maxIndex();
        Process temp;
        if(index == -1 && process.getSize() <= getFree()){
            getList().add(process);
            process.setBase(getPointer());
            setPointer(getPointer()+process.getSize());
            setUsed(getUsed()+process.getSize());
            System.out.println("Done!");
        }else if(process.getSize()<=getList().get(index).getSize()){
            getList().get(index).setName(process.getName());
            if(process.getSize() < getList().get(index).getSize()){
                temp = new Process("Empty", getList().get(index).getSize() - process.getSize());
                getList().add(index+1, temp);
                getList().get(index+1).setBase(getList().get(index).getBase()+getList().get(index).getSize());
                }
            getList().get(index).setSize(process.getSize());
            System.out.println("Done!");
        }else{
            System.err.println("No memory available.");
        }
    }
    
    private int maxIndex(){
        int index = -1;
        int max = getMaxSize()-getPointer();
        Process temp;
        for (int i = 0; i < getList().size(); i++) {
            temp = getList().get(i);
            if(temp.getSize()> max && temp.getName().equalsIgnoreCase("Empty")){
                max = temp.getSize();
                index = i;
            }
        }
        return index;
    }
    private int minIndex(int size){
        int index = -1;
        int min = 2147483647;
        Process temp;
        for (int i = 0; i < getList().size(); i++) {
            temp = getList().get(i);
            if(temp.getSize()< min && temp.getSize()>= size){
                if(temp.getName().equalsIgnoreCase("Empty")){
                    index = i;
                    min = getList().get(index).getSize();
                }
            }
        }
        if(getMaxSize()-getPointer()>= size && getMaxSize()-getPointer() < min){
            index = -1;
        }
        return index;
    }
    
    private boolean emptyExist() {
        boolean condition = false;
        for (int i = 0; i < getList().size(); i++) {
            if(getList().get(i).getName().equalsIgnoreCase("Empty")){
                condition = true;
            }
        }
        return condition;
    }
    
    private int getMaxSize() {
        return maxSize;
    }

    private void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    private int getFree() {
        return getMaxSize()-getUsed();
    }


    private int getUsed() {
        return used;
    }

    private void setUsed(int used) {
        this.used = used;
    }

    private int getPointer() {
        return pointer;
    }

    private void setPointer(int pointer) {
        this.pointer = pointer;
    }

    private ArrayList<Process> getList() {
        return list;
    }
}
