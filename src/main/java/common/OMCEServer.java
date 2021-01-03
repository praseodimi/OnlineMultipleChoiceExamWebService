package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OMCEServer extends Remote {
    void registerStudent(OMCEClient student, String universityID) throws RemoteException;

    void notifyStartExam() throws RemoteException;

    String getFilePath(String message) throws RemoteException;

    int getNumStudents() throws RemoteException;

    boolean isExamStarted() throws RemoteException;

    boolean isStudentExamFinished(String studentId) throws RemoteException;

    void generateStudentExams(String csvPath) throws RemoteException;

    void sendAnswer(String studentId, String answerNum) throws RemoteException;

    void send() throws RemoteException;

    void createResultsFile(String csvPath) throws RemoteException;

    void sendResults() throws RemoteException;

    boolean isCsvPathFile(String csvPath) throws RemoteException;

    boolean isCsvPathDirectory(String csvPath) throws RemoteException;

    void notifyStudentLeaved(String studentId) throws RemoteException;
}
