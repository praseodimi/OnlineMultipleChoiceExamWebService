package omce.ws.entities;

import omce.ws.utils.StudentGradeKey;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "StudentGrades")
public class StudentGrade {

    public StudentGradeKey getId() {
        return id;
    }

    public void setId(StudentGradeKey id) {
        this.id = id;
    }

    @EmbeddedId
    @Id
    StudentGradeKey id;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    Student student;

    @ManyToOne
    @MapsId("examId")
    @JoinColumn(name = "exam_id")
    Exam exam;

    private String grade;

    public StudentGrade(Student student, Exam exam, String grade) {
        this.student = student;
        this.exam = exam;
        this.grade = grade;
        this.id = new StudentGradeKey(student.getStudentId(), exam.getExamId());
    }

    public StudentGrade() {

    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentGrade that = (StudentGrade) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(student, that.student) &&
                Objects.equals(exam, that.exam) &&
                Objects.equals(grade, that.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, student, exam, grade);
    }
}
