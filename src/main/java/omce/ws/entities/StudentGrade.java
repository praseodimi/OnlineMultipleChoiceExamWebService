package omce.ws.entities;

import omce.ws.utils.StudentGradeKey;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "StudentGrades")
public class StudentGrade {

    @EmbeddedId
    @Id
    StudentGradeKey id;

    @ManyToOne
    @MapsId("examId")
    @JoinColumn(name = "exam_id")
    Exam exam;

    private String grade;

    public StudentGrade(String universityId, Exam exam, String grade) {
        this.exam = exam;
        this.grade = grade;
        this.id = new StudentGradeKey(universityId, exam.getExamId());
    }

    public StudentGrade() {

    }

    public StudentGradeKey getId() {
        return id;
    }

    public void setId(StudentGradeKey id) {
        this.id = id;
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
                Objects.equals(exam, that.exam) &&
                Objects.equals(grade, that.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, exam, grade);
    }
}
