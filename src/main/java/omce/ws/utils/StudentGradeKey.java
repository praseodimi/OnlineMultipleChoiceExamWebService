package omce.ws.utils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StudentGradeKey implements Serializable {

    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "exam_id")
    private Long examId;

    public StudentGradeKey(Long studentId, Long examId) {
        this.studentId = studentId;
        this.examId = examId;
    }

    public StudentGradeKey() {

    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentGradeKey that = (StudentGradeKey) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(examId, that.examId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, examId);
    }
}
