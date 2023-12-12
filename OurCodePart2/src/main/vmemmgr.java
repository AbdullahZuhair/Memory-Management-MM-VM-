package main;





/*

Name: 
Mohammed Alghamdi
Abdullah Almontashri
Abdulmajed Almalki
Tamim Alhumaydhi


Compiler Version: NetBeans IDE 8.2 (Build 201609300101)

Java: 1.8.0_131; Java HotSpot(TM) 64-Bit Server VM 25.131-b11

Runtime: Java(TM) SE Runtime Environment 1.8.0_131-b11

System: Windows 10 version 10.0 running on amd64; en_US (nb)

*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class vmemmgr {

    int ARGC_ERROR = 1;
    int FILE_ERROR = 2;
    int BUFLEN = 256;
    int FRAME_SIZE = 256;

    byte[] main_mem = new byte[65536];
    char[] main_mem_fifo = new char[32768];
    int[] page_queue = new int[128];
    int qhead = 0, qtail = 0;
    //TLB array
    int[][] tlb = new int[16][2];
    int current_tlb_entry = 0;
    // Page table array
    int[] page_table = new int[256];

    int currentFrame = 0;
    int pageFaultCount = 0;
    int tlbHitCount = 0;
    int accessCount = 0;

    //Default addresess.txt,BACKING_STORE.bin path
    String defaultFilepath = System.getProperty("user.dir") + "\\src\\main\\addresses.txt";
    String defaultBackingFile = System.getProperty("user.dir") + "\\src\\main\\BACKING_STORE.bin";
    String addressesFilePath;
    String BackingFilePath;

    public void run(String argFilePath) throws FileNotFoundException, IOException {

        if (argFilePath != null) {
            //dist
            addressesFilePath = System.getProperty("user.dir") + "\\" + argFilePath;
            BackingFilePath = System.getProperty("user.dir") + "\\BACKING_STORE.bin";
        } else {
            //src
            addressesFilePath = defaultFilepath;
            BackingFilePath = defaultBackingFile;

        }

        Arrays.fill(page_table, -1);
        try {
            //Read addresses in addresses.txt into ArrayList
            ArrayList<String> addresses = readFile(argFilePath);
            for (int i = 0; i < addresses.size(); i++) {
                int physicalAddress = getPhysicalMemory(addresses.get(i));

                //Print Logical Address,Physical Address and signed byte value
                System.out.println("-----\nLogical Address: " + addresses.get(i) + "\nPhysical Address: " + physicalAddress + "\nValue: " + main_mem[physicalAddress - 1]);
            }
            //all addresses is complete. print statistics.
            System.out.println("-----statistics-----");
            System.out.println("Page faults = " + pageFaultCount);
            System.out.println("TLB hits = " + tlbHitCount);

            System.out.println("Page-fault rate = " + (double) (100 * (double) pageFaultCount / (double) accessCount) + "%");
            System.out.println("TLB hit rate = " + (double) (100 * (double) tlbHitCount / (double) accessCount) + "%");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(vmemmgr.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Only call updateTLB when we know we have a page fault, so we dont need to recheck the TLB if it has our new entry.
    public void updateTLB(int pageNumber, int frame) {
        if (current_tlb_entry == 16) {
            current_tlb_entry = 0; //Reset tlb to start at top again
        }
        tlb[current_tlb_entry] = new int[]{pageNumber, frame};
        current_tlb_entry++;
    }

    //Converts the virtual memory address to physicalAddress. Does necessary paging if necessary.
    public int getPhysicalMemory(String address) throws IOException {
        accessCount++;
        int physicalAddress = -1;
        int pageNumber = getPage(address);
        int offset = getOffset(address);

        int frameNumber = checkTlb(pageNumber);

        if (frameNumber != -1) { //TLB hit
            physicalAddress = (frameNumber * FRAME_SIZE) + offset;
            tlbHitCount++;

        } else { //TLB miss. Now check page table

            if (page_table[pageNumber] == -1) { //Page table miss. Means we have a page fault.

                byte[] page = getPageFromBin(pageNumber);
                //now need to add this page to main_mem at the spot of current_frame
                int physicalAddressStartFrame = (currentFrame * FRAME_SIZE);
                for (int i = 0; i < page.length; i++) {
                    main_mem[physicalAddressStartFrame + i] = page[i];
                }

                physicalAddress = (currentFrame * FRAME_SIZE) + offset;

                updateTLB(pageNumber, currentFrame);

                //Update page table too
                page_table[pageNumber] = currentFrame;

                currentFrame = (currentFrame + 1) % FRAME_SIZE;
                //frame is stored (and the page table and TLB are updated)

            } else { //page table hit.
                frameNumber = page_table[pageNumber];
                physicalAddress = (frameNumber * FRAME_SIZE) + offset;
                updateTLB(pageNumber, frameNumber);
            }

        }

        return physicalAddress;

    }

    //This reads the page from the backing store , given a page number to find.
    //The backing store is represented by the file BACKING_STORE.bin, a binary file of size 65,536 bytes.
    public byte[] getPageFromBin(int pageNumber) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(new File(BackingFilePath));
        int start = pageNumber * 256; //Start this many bytes in.
        int end = start + 256;
        byte[] page = new byte[256];
        int index = 0;
        for (int i = 0; i < end; i++) { //count all the way up till end of desired page
            byte data = (byte) fis.read();
            if (i > start && i < end) {
                page[index] = data;
                index++;
            }
        }
        fis.close();
        pageFaultCount++;
        return page;
    }

    //check to see if page number is in the TLB. Returns -1 if not.
    public int checkTlb(int pageNumber) {
        for (int i = 0; i < tlb.length; i++) {
            if (tlb[i][0] == pageNumber) {
                return tlb[i][1]; //TLB hit. Return frame number.
            }
        }
        return -1; //TLB miss. Return -1
    }

    public ArrayList<String> readFile(String argFilePath) throws FileNotFoundException {
        File file = new File(addressesFilePath);
        Scanner reader = new Scanner(file);
        ArrayList<String> stringArr = new ArrayList<String>();
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            //ignore empty lines
            if (line == null || line.trim().length() == 0) {
                continue;
            }
            stringArr.add(line);
        }
        return stringArr;
    }

    public int getPage(String address) {
        int input = parseInt(address);
        return (input >>> 8) & 0b11111111;
    }

    public int getOffset(String address) {
        int input = parseInt(address);
        return input & 0b11111111;
    }
}
