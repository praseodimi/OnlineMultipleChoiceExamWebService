package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface OMCEClient extends Remote {
    void notifyStartExam() throws RemoteException;

    void notifyRegisterStudent() throws RemoteException;

    boolean isRegistered() throws RemoteException;

    String getStudentId() throws RemoteException;

    void notifyQuiz(String quiz) throws RemoteException;

    String inputAnswer() throws RemoteException;

    void notifyResult(String result) throws RemoteException;

    void notifyRegisteredStudent() throws RemoteException;

    boolean isCorrectId(String id) throws RemoteException;

    boolean isCorrectAnswer(String answer) throws RemoteException;

    void leaveSession() throws RemoteException;

    void setAnswer(String answer) throws RemoteException;

    String getAnswer() throws RemoteException;

    void setExamFinished(boolean examFinished) throws RemoteException;

    boolean isExamFinished() throws RemoteException;

    void checkStartExam(OMCEServer server) throws RemoteException;
}
