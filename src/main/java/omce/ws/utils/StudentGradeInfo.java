package omce.ws.utils;

public class StudentGradeInfo {

    private String universityId;
    private Long examId;
    private String grade;

    public StudentGradeInfo(String universityId, Long examId, String grade) {
        this.universityId = universityId;
        this.examId = examId;
        this.grade = grade;
    }

    public StudentGradeInfo() {

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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
