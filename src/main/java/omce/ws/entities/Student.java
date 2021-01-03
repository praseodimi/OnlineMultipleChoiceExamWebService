package omce.ws.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "Students")
public class Student implements Serializable {

    @Id
    @Column(name = "student_id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long studentId;
    private String universityId;


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    public Set<StudentGrade> grades;

    public Student(String universityId) {
        this.universityId = universityId;
    }

    public Student() {

    }

    public Set<StudentGrade> getGrades() {
        return grades;
    }

    public void setGrades(Set<StudentGrade> grades) {
        this.grades = grades;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getUniversityId() {
        return universityId;
    }

    public void setUniversityId(String universityId) {
        this.universityId = universityId;
    }
}