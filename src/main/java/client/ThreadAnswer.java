package client;

import common.OMCEClient;

import java.rmi.RemoteException;

/**
 * This class has been implemented to obtain the answer
 * passing it through stdin and then send it to the server
 */
public class ThreadAnswer extends Thread {

    private OMCEClient client;
    private String answer;

    public ThreadAnswer(OMCEClient client) {
        this.client = client;
    }

    public void run() {
        try {
            do {
                answer = client.inputAnswer();
            } while (!client.isCorrectAnswer(answer));

            client.setAnswer(answer);
            if (client.isExamFinished())
                client.leaveSession();
            synchronized (this.client) {
                this.client.notify();
            }
        } catch (RemoteException e) {
            System.out.println("Exam session is not reachable");
            System.exit(0);
        }

    }


}