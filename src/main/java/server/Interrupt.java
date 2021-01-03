package server;

import java.util.Scanner;

public class Interrupt extends Thread {
    String interrupt_key;
    Object semaphore;
    //variable to change when start
    private boolean interrupted = false;

    //semaphore must be the syncronized object
    public Interrupt(Object semaphore, String interrupt_key) {
        this.semaphore = semaphore;
        this.interrupt_key = interrupt_key;
    }

    public void run() {
        while (true) {
            //read the key
            Scanner scanner = new Scanner(System.in);
            String x = scanner.nextLine();
            if (x.equals(this.interrupt_key)) {
                //if is the key we expect, change the variable, notify and return(finish thread)
                synchronized (this.semaphore) {
                    interrupted = true;
                    this.semaphore.notify();
                    return;
                }
            }
        }
    }

    @Override
    public boolean isInterrupted() {
        return interrupted;
    }

}
