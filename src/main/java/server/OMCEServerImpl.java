package server;

import omce.ws.entities.Exam;
import common.OMCEClient;
import common.OMCEServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class OMCEServerImpl extends UnicastRemoteObject implements OMCEServer {

    private HashMap<String, OMCEClient> students = new HashMap<>();
    private HashMap<String, Exam> studentExams = new HashMap<>();
    private List<String> error_students = new ArrayList<>();
    boolean isExamStarted = false;
    private String studentToNotify = "ALL";

    public OMCEServerImpl() throws RemoteException {
    }

    /**
     * Check if the student is registered, otherwise it is registered
     */
    public void registerStudent(OMCEClient student, String studentId) {
        if (students.containsKey(studentId)) {
            anotherStudentRegistered(student);
        } else {
            registerNewStudent(student, studentId);
        }
    }

    /**
     * Stores the student into a Hashmap and notifies to the student that has registered.
     * In case it cannot establish a connection with the student, it removes it.
     */
    private void registerNewStudent(OMCEClient student, String studentId) {
        synchronized (this) {
            System.out.println("Registering student " + studentId);
            students.put(studentId, student);
            try {
                student.notifyRegisterStudent();
                this.notify();
            } catch (RemoteException e) {
                System.out.println(studentId + " is not reachable to registering.");
                removeStudent(studentId);
            }
        }
    }

    /**
     * Notifies that there is already a student with that id registered
     */
    private void anotherStudentRegistered(OMCEClient student) {
        try {
            student.notifyRegisteredStudent();
        } catch (RemoteException e) {
        }
    }

    /**
     * Notifies all the students that the exam is going to start.
     * In case it cannot establish a connection with the students, it removes them.
     */
    public void notifyStartExam() {
        isExamStarted = true;
        for (HashMap.Entry<String, OMCEClient> s : students.entrySet()) {
            try {
                s.getValue().notifyStartExam();
            } catch (RemoteException e) {
                System.out.println(s.getKey() + " is not reachable to starting the exam.");
                error_students.add(s.getKey());
            }
        }
        removeStudents();
    }

    /**
     * Generates a Hashmap with a copy of the exam for each student registered
     */
    public void generateStudentExams(String csvPath) {
        for (HashMap.Entry<String, OMCEClient> s : students.entrySet()) {
            studentExams.put(s.getKey(), ExamGenerator.generateExam(csvPath));
        }
    }

    /**
     * Gets the absolute path of the file from stdin
     */
    public String getFilePath(String message) {
        Scanner keyboard = new Scanner(System.in);
        System.out.println(message);
        return keyboard.nextLine();
    }

    /**
     * Check if the absolute path is a file
     */
    public boolean isCsvPathFile(String csvPath) {
        File file = new File(csvPath);
        return file.isFile();
    }

    /**
     * Check if the absolute path is a directory
     */
    public boolean isCsvPathDirectory(String csvPath) {
        File file = new File(csvPath);
        return file.isDirectory();
    }

    public int getNumStudents() {
        return students.size();
    }

    public boolean isExamStarted() {
        return isExamStarted;
    }

    /**
     * Check if the student has finished the exam
     */
    public boolean isStudentExamFinished(String studentId) {
        Exam exam = studentExams.get(studentId);
        return exam.isFinished();
    }

    /**
     * Manages the sending of quizzes to the students and
     * if there is one not reachable it removes it from the Hashmap
     */
    public void send() {
        error_students = new ArrayList<>();
        if (studentToNotify.equals("ALL"))
            sendQuizzes();
        else
            sendQuiz();
        removeStudents();
    }

    private void sendQuizzes() {
        for (HashMap.Entry<String, OMCEClient> s : students.entrySet()) {
            sendQuizTo(s.getKey(), s.getValue());
        }
    }

    private void sendQuiz() {
        sendQuizTo(studentToNotify, students.get(studentToNotify));
    }

    /**
     * Sends a quiz or the result of the exam to the student.
     * In case it cannot establish a connection with the student, it removes it.
     */
    private void sendQuizTo(String studentId, OMCEClient student) {
        try {

            Exam exam = studentExams.get(studentId);
            // Get the next quiz to send
            Quiz nextQuiz = exam.getNextQuiz();
            if (nextQuiz != null) {
                students.get(studentId).notifyQuiz(nextQuiz.toString());
            } else {
                exam.setFinished(true);
                // Get the result of the exam
                String result = exam.calculateResult();
                student.notifyResult(result);
                System.out.println("Student " + studentId + " has finished the exam.");
                removeStudent(studentId);
            }
        } catch (RemoteException e) {
            System.out.println(studentId + " is not reachable to send quiz.");
            error_students.add(studentId);
        }
    }

    /**
     * Receives the student's answer and saves it in the student's exam
     */
    public void sendAnswer(String studentId, String answerNum) {
        synchronized (this) {
            Exam exam = studentExams.get(studentId);
            // Get the next quiz to send
            Quiz quiz = exam.getNextQuiz();
            quiz.setSelectedChoice(Integer.parseInt(answerNum));
            // Updates the exam with the received answer
            exam.setQuiz(quiz);
            studentExams.put(studentId, exam);
            studentToNotify = studentId;
            this.notify();
        }
    }

    /**
     * Sends the result of the exam to students.
     * In case it cannot establish a connection with the students, it removes them.
     */
    public void sendResults() {
        error_students = new ArrayList<>();
        for (HashMap.Entry<String, OMCEClient> s : students.entrySet()) {
            try {
                Exam exam = studentExams.get(s.getKey());
                exam.setFinished(true);
                // Get the result of the exam
                String result = exam.calculateResult();
                s.getValue().notifyResult(result);
            } catch (RemoteException e) {
                System.out.println(s.getKey() + " is not reachable to send result.");
                error_students.add(s.getKey());
            }
        }
        removeStudents();
    }

    /**
     * Creates the output file where it stores all the id students with their grade
     */
    public void createResultsFile(String csvPath) {
        ArrayList<String[]> studentGrades = new ArrayList<>();
        // Add the title of the columns
        studentGrades.add(new String[]{"UniversityID", "Grade"});

        for (HashMap.Entry<String, Exam> s : studentExams.entrySet()) {
            studentGrades.add(new String[]{s.getKey(), s.getValue().getResult()});
        }

        // Add the filename to the absolute path
        csvPath += "/results.csv";

        File csvOutputFile = new File(csvPath);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            // Prints each line to the output file
            for (String[] studentGrade : studentGrades) {
                pw.println(convertToCSV(studentGrade));
            }
            System.out.println("Results has been stored in results.csv file.");
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }

    /**
     * Creates a string from string array
     */
    private String convertToCSV(String[] data) {
        return String.join(";", data);
    }

    /**
     * For each students remove the student who has error in the connection
     */
    private void removeStudents() {
        for (String s : error_students) {
            removeStudent(s);
        }
    }

    /**
     * Removes students and prints the remaining students registered.
     */
    private void removeStudent(String s) {
        this.students.remove(s);
        System.out.println("There are " + getNumStudents() + " remaining students");
    }

    /**
     * Notify to the server the student who has leaved the exam.
     */
    public void notifyStudentLeaved(String studentId) {
        System.out.println("Student " + studentId + " has leaved the exam.");
        removeStudent(studentId);
    }
}