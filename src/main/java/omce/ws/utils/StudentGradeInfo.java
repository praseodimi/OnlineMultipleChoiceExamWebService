package omce.ws.utils;

public class StudentGradeInfo {

    private Long studentId;
    private Long examId;
    private String grade;

    public StudentGradeInfo(Long studentId, Long examId, String grade) {
        this.studentId = studentId;
        this.examId = examId;
        this.grade = grade;
    }

    public StudentGradeInfo(){

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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
