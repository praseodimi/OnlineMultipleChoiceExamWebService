package rmi.server;

import rmi.common.OMCEServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    private static Registry startRegistry(Integer port)
            throws RemoteException {
        if (port == null) {
            port = 1099;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list();
            // The above call will throw an exception
            // if the registry does not already exist
            return registry;
        } catch (RemoteException ex) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located ");
            Registry registry = LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created at port ");
            return registry;
        }
    }

    public static void main(String[] args) {
        try {
            Registry registry = startRegistry(null);
            OMCEServer server = new OMCEServerImpl();

            String csvPath;
            // Until the stdin is an absolute path file
            do {
                // Read the route of .csv file
                csvPath = server.getFilePath("Please, enter the absolute route of .csv exam file.");
            } while (!server.isCsvPathFile(csvPath));

            server.uploadExamToWS(csvPath);
            System.out.println("The exam is uploaded correctly");

            registry.bind("Hello", server);

            synchronized (server) {
                String start_word = "start";
                Interrupt interruptStart = new Interrupt(server, start_word);
                //The tread starts reading for the key
                interruptStart.start();

                // As long as the start word is not written
                while (!interruptStart.isInterrupted()) {
                    System.out.println("Students registered " + server.getNumStudents());
                    System.out.println("Write \"" + start_word + "\" to start the exam");
                    server.wait();
                    //rmi.server.wait can be notified from the interrupt key, or the remote object implemented
                }

                // Generates exams and notifies students of exam start
                server.generateStudentExams(csvPath);
                server.notifyStartExam();
                System.out.println("Starting exam.");

                String finish_word = "finish";
                Interrupt interruptFinish = new Interrupt(server, finish_word);
                //The tread starts reading for the key
                interruptFinish.start();
                System.out.println("Write \"" + finish_word + "\" to finish the exam");

                // As long as the finish word is not written
                while (!interruptFinish.isInterrupted()) {
                    // Sends a quiz to the student
                    server.send();
                    server.wait();
                }
                System.out.println("Exam session finished.");
                server.sendResults();

                // Until the stdin is an absolute path directory
                do {
                    csvPath = server.getFilePath("Please, enter the absolute path directory to store results file.");
                } while (!server.isCsvPathDirectory(csvPath));

                // Creates the results.csv
                server.createResultsFile(csvPath);
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
