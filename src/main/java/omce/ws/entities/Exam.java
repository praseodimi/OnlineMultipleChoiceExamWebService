package omce.ws.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import server.Quiz;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "exams")
public class Exam implements Serializable {

    @Id
    @Column(name = "exam_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long examId;
    @Transient
    @JsonIgnore
    private ArrayList<Quiz> quizzes;
    @Transient
    @JsonIgnore
    private boolean isFinished;
    @Transient
    @JsonIgnore
    private String result = "0.0";
    private String description;
    private String date;
    private String time;
    private String location;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<StudentGrade> grades;

    public ArrayList<Quiz> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(ArrayList<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Set<StudentGrade> getGrades() {
        return grades;
    }

    public void setGrades(Set<StudentGrade> grades) {
        this.grades = grades;
    }

    public Exam(String description, String date, String time, String location) {
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public Exam() {

    }

    public Exam(ArrayList<Quiz> quizzes) {
        this.quizzes = quizzes;
        this.isFinished = false;
    }

    @JsonIgnore
    public Quiz getNextQuiz() {
        for (Quiz q : quizzes) {
            if (q.getSelectedChoice() == null) {
                return q;
            }
        }
        return null;
    }

    public void setQuiz(Quiz quiz) {
        quizzes.set(quiz.getId(), quiz);
    }

    public String calculateResult() {
        int correct = 0;

        // For each quiz check if the correct answer matches with selected choice
        for (Quiz q : quizzes) {
            if (q.getSelectedChoice() != null && q.getSelectedChoice() == q.getCorrectAnswer()) {
                correct++;
            }
        }

        result = String.valueOf(((float) correct / quizzes.size()) * 10.0);

        return result;
    }

    @JsonIgnore
    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getResult() {
        return result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}
