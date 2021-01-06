package rmi.server;

import omce.ws.entities.Exam;
import rmi.common.OMCEClient;
import rmi.common.OMCEServer;
import omce.ws.entities.Student;
import omce.ws.utils.StudentGradeInfo;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
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
    public void registerStudent(OMCEClient student, String universityId) {
        if (getStudentInWS(universityId) != null) {
            anotherStudentRegistered(student);
        } else {
            registerNewStudent(student, universityId);
        }

//        if (students.containsKey(universityId)) {
//            anotherStudentRegistered(student);
//        } else {
//            registerNewStudent(student, universityId);
//        }
    }

    private Student getStudentInWS(String universityId) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/getStudent/"+universityId;
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<Student> response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Student.class);
            return response.getBody();
        } catch (URISyntaxException e) {
            System.out.println("Error");
        }catch (HttpClientErrorException e) {
            System.out.println("Exam already in Web Service");
        }

        return null;
    }

    /**
     * Stores the student into a Hashmap and notifies to the student that has registered.
     * In case it cannot establish a connection with the student, it removes it.
     */
    private void registerNewStudent(OMCEClient student, String universityId) {
        synchronized (this) {
            System.out.println("Registering student " + universityId);
            students.put(universityId, student);
            createStudentWS(universityId);
            try {
                student.notifyRegisterStudent();
                this.notify();
            } catch (RemoteException e) {
                System.out.println(universityId + " is not reachable to registering.");
                removeStudent(universityId);
            }
        }
    }

    private void createStudentWS(String universityId) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/createStudent/" + universityId;
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Student> httpEntity = new HttpEntity<>(new Student(universityId), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(uri,httpEntity, String.class);
            System.out.println(response.getStatusCode());
        } catch (URISyntaxException e) {
            System.out.println("Error");
        }catch (HttpClientErrorException e) {
            System.out.println("Student already in Web Service");
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
        Exam exam = ExamGenerator.generateExam(csvPath);
        Exam examDB = createExamWS(exam);
        if (examDB == null){
            System.out.println("Error getting the exam from WS");
            System.exit(0);
        }
        for (HashMap.Entry<String, OMCEClient> s : students.entrySet()) {
            Exam e = ExamGenerator.generateExam(csvPath);
            e.setExamId(examDB.getExamId());
            studentExams.put(s.getKey(), e);
        }
    }

    private Exam createExamWS(Exam exam) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/createExam";
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Exam> httpEntity = new HttpEntity<>(exam, headers);
            ResponseEntity<Exam> response = restTemplate.postForEntity(uri,httpEntity, Exam.class);
            return response.getBody();
        } catch (URISyntaxException | HttpClientErrorException e) {
            System.out.println("Error in WS");
        }
        return null;
    }

    private Exam getExamByContent(Exam exam) {
        Exam examDB = null;
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/searchExamByContent";
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Exam> httpEntity = new HttpEntity<>(exam, headers);
            ResponseEntity<Exam> response = restTemplate.postForEntity(uri,httpEntity, Exam.class);
            examDB = response.getBody();
        }  catch (URISyntaxException e) {
            System.out.println("Error");
        }
        return examDB;
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
    public boolean isStudentExamFinished(String universityId) {
        Exam exam = studentExams.get(universityId);
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
    private void sendQuizTo(String universityId, OMCEClient student) {
        try {
            Exam exam = studentExams.get(universityId);
            // Get the next quiz to send
            Quiz nextQuiz = exam.getNextQuiz();
            if (nextQuiz != null) {
                students.get(universityId).notifyQuiz(nextQuiz.toString());
            } else {
                exam.setFinished(true);
                // Get the result of the exam
                String result = exam.calculateResult();
                createGradeWS(universityId, exam.getExamId(), result);
                student.notifyResult(result);
                System.out.println("Student " + universityId + " has finished the exam.");
                removeStudent(universityId);
            }
        } catch (RemoteException e) {
            System.out.println(universityId + " is not reachable to send quiz.");
            error_students.add(universityId);
        }
    }

    private void createGradeWS(String universityId, Long examId, String grade) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/uploadGrades";
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            ArrayList<StudentGradeInfo> list = new ArrayList<>();
            list.add(new StudentGradeInfo(universityId, examId, grade));
            HttpEntity<ArrayList<StudentGradeInfo>> httpEntity = new HttpEntity<>(list , headers);
            ResponseEntity<String> response = restTemplate.postForEntity(uri,httpEntity, String.class);
            System.out.println(response.getStatusCode());

        } catch (URISyntaxException e) {
            System.out.println("Error");
        }catch (HttpClientErrorException e) {
            System.out.println("Student already in Web Service");
        }
    }

    /**
     * Receives the student's answer and saves it in the student's exam
     */
    public void sendAnswer(String universityId, String answerNum) {
        synchronized (this) {
            Exam exam = studentExams.get(universityId);
            // Get the next quiz to send
            Quiz quiz = exam.getNextQuiz();
            quiz.setSelectedChoice(Integer.parseInt(answerNum));
            // Updates the exam with the received answer
            exam.setQuiz(quiz);
            studentExams.put(universityId, exam);
            studentToNotify = universityId;
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
                createGradeWS(s.getKey(), exam.getExamId(), result);
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
        removeStudentWS(s);
        System.out.println("There are " + getNumStudents() + " remaining students");
    }

    private void removeStudentWS(String universityId) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String string = "http://localhost:8080/omcews/deleteStudent/"+universityId;
            URI uri = new URI(string);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.DELETE, httpEntity, String.class);
        } catch (URISyntaxException | HttpClientErrorException e) {
            System.out.println("Error in WS");
        }
    }

    /**
     * Notify to the rmi.server the student who has leaved the exam.
     */
    public void notifyStudentLeaved(String universityId) {
        System.out.println("Student " + universityId + " has leaved the exam.");
        removeStudent(universityId);
    }
}