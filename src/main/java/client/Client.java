package client;

import common.OMCEClient;
import common.OMCEServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            OMCEClient client = new OMCEClientImpl();
            OMCEServer server = (OMCEServer) registry.lookup("Hello");

            // Check to see if the exam has already started
            client.checkStartExam(server);

            String studentId;
            // As long as the student has not registered
            do {
                // As long as the studentId is not alphanumeric
                do {
                    studentId = client.getStudentId();
                } while (!client.isCorrectId(studentId));

                // Check to see if the exam has already started
                client.checkStartExam(server);
                // Registers the student
                server.registerStudent(client, studentId);
            } while (!client.isRegistered());

            synchronized (client) {

                String leave_key = "leave";

                // Wait until exam starts
                // Timeout if server does not start the exam within 10 minutes
                client.wait(600000);

                // While the exam session has not finished
                while (!server.isStudentExamFinished(studentId)) {
                    // Thread to get the answer from stdin and send it to the server
                    ThreadAnswer thread = new ThreadAnswer(client);
                    thread.start();
                    // Waiting for the next quiz or result
                    client.wait();
                    // Check if the server writes finish
                    if ((server.isStudentExamFinished(studentId))) {
                        // Changes the examFinished state if the server finish the exam
                        System.out.println("Enter \"leave\" to leave the exam");
                        client.setExamFinished(true);
                        break;
                    }
                    if (client.getAnswer().equals(leave_key)) {
                        // Notify server he leaves.
                        server.notifyStudentLeaved(studentId);
                        System.exit(0);
                    }
                    server.sendAnswer(studentId, client.getAnswer());
                    // Timeout for next quiz waiting is 5 minutes in case of bad connection with server
                    client.wait(300000);
                }

                if (!client.isExamFinished()) {
                    // Student leaves the sesion
                    client.leaveSession();
                }
            }
        } catch (Exception e) {
            System.out.println("Exam session is not reachable.");
            System.exit(0);
        }
    }
}
