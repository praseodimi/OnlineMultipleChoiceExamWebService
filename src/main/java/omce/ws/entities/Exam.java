package omce.ws.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "exams")
public class Exam implements Serializable {

    @Id
    @Column(name = "exam_id", nullable = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long examId;

    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String date;
    @Column(nullable = false)
    private String time;
    @Column(nullable = false)
    private String location;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<StudentGrade> grades;

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

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
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
