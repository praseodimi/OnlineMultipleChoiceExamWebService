package omce.ws.utils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StudentGradeKey implements Serializable {

    @Column(name = "university_id")
    private String universityId;
    @Column(name = "exam_id")
    private Long examId;

    public StudentGradeKey(String universityId, Long examId) {
        this.universityId = universityId;
        this.examId = examId;
    }

    public StudentGradeKey() {

    }

    public String getUniversityId() {
        return universityId;
    }

    public void setUniversityId(String universityId) {
        this.universityId = universityId;
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
        return Objects.equals(universityId, that.universityId) &&
                Objects.equals(examId, that.examId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(universityId, examId);
    }
}
