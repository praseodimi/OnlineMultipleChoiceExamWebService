package omce.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import omce.ws.entities.Exam;
import omce.ws.entities.Student;
import omce.ws.entities.StudentGrade;
import omce.ws.repositories.ExamRepository;
import omce.ws.repositories.StudentGradeRepository;
import omce.ws.repositories.StudentRepository;
import omce.ws.utils.StudentGradeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class OMCEWebController {

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private StudentGradeRepository studentGradeRepository;
    @Autowired
    private StudentRepository studentRepository;

    /* Basic Functions */

    // The web service generates a unique key for the new uploaded exam if it does not exist in the global directory (WS).
    // Store in the global directory (WS) exams description, date, time and location.
    // POST

    @PostMapping("/createExam")
    public ResponseEntity<?> createExam(@RequestBody Exam exam) {

        Exam examDB = examRepository.findByContent(exam.getDescription(),
                exam.getDate(), exam.getTime(), exam.getLocation());
        if (examDB != null)
            return ResponseEntity.status(HttpStatus.OK).body(examDB);
        Exam e = examRepository.save(exam);
        return ResponseEntity.status(HttpStatus.CREATED).body(e);
    }

    // Modify exam’s description.
    // PUT
    @PutMapping("/updateExam/{examId}")
    public ResponseEntity updateExam(@PathVariable Long examId, @RequestBody Exam exam) {
        Optional<Exam> examToEdit = examRepository.findById(examId);
        if (!examToEdit.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam is not present");
        examToEdit.get().setDescription(exam.getDescription());
        examRepository.save(examToEdit.get());
        return ResponseEntity.status(HttpStatus.OK).body("Exam updated correctly");
    }

    // Delete exam (if it has no grades).
    // DELETE
    @DeleteMapping("/deleteExam/{examId}")
    public ResponseEntity deleteExam(@PathVariable Long examId, @RequestBody Exam exam) {
        Optional<Exam> examToDelete = examRepository.findById(examId);
        if (!examToDelete.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam is not present");

        examRepository.delete(examToDelete.get());
        return ResponseEntity.status(HttpStatus.OK).body("Exam deleted correctly");
    }


    // Search exam content using the key or search them using textual description (full/partial search).
    // GET
    @GetMapping(value = "/searchExamById/{id}")
    public ResponseEntity<Exam> searchExamById(@PathVariable Long id) {
        Optional<Exam> exam = examRepository.findById(id);
        if (!exam.isPresent())
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(exam.get());
    }

    @GetMapping("/searchExamByDescription/{description}")
    public ResponseEntity<?> searchExamByDescription(@PathVariable String description) {
        List<Exam> exams = examRepository.findByDescription(description);

        if (exams.size() == 0)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam is not present");

        return ResponseEntity.ok(exams);
    }


    // Download exams information. By Id or listing all the contents in the system.
    // GET

    @GetMapping("/downloadExam/{examId}")
    public ResponseEntity<?> downloadExamById(@PathVariable Long examId) {

        Optional<Exam> exam = examRepository.findById(examId);
        if (!exam.isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading the exam");

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(exam.get());
            byte[] examBytes = json.getBytes();
            String fileName = "exam" + examId + ".json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading the exam");
        }

    }

    @GetMapping("/downloadExams")
    public ResponseEntity<?> downloadExams() {

        List<Exam> exams = examRepository.findAll();
        if (exams.size() == 0)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("no hi ha exams");

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(exams);
            byte[] examBytes = json.getBytes();
            String fileName = "exams.json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading the exams");
        }
    }

    /* Advanced Functions */

    // Upload grades to an exam.
    // POST
    @PostMapping("/uploadGrades")
    public ResponseEntity uploadGrades(@RequestBody List<StudentGradeInfo> studentGrades) {

        if (studentGrades.size() == 0)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        try{
            for (StudentGradeInfo sg: studentGrades) {
                StudentGrade studentGrade = new StudentGrade(
                        studentRepository.findById(sg.getStudentId()).get(),
                        examRepository.findById(sg.getExamId()).get(),
                        sg.getGrade());
                studentGradeRepository.save(studentGrade);
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exam or student does not exist");
        }
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    // Download student’s grades.
    // GET
    @GetMapping("/downloadStudentGrades/{studentId}")
    public ResponseEntity<?> downloadStudentGrades(@PathVariable Long studentId) {

        List<StudentGrade> studentGrades = studentGradeRepository.findByStudentId(studentId);
        if (studentGrades.size() == 0)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("no hi ha StudentGrades");

        ArrayList<StudentGradeInfo> studentGradesInfo = new ArrayList<>();
        for (StudentGrade sg : studentGrades) {
            StudentGradeInfo studentGradeInfo = new StudentGradeInfo(sg.getStudent().getStudentId(),
                    sg.getExam().getExamId(), sg.getGrade());
            studentGradesInfo.add(studentGradeInfo);
        }

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(studentGradesInfo);
            byte[] examBytes = json.getBytes();
            String fileName = "Grades" + studentId + ".json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading grades");
        }

    }

    // Manage student’s access (by ID).
    // POST
    @PostMapping("/createStudent/{universityId}")
    public ResponseEntity createStudent(@PathVariable String universityId) {

        Student student = studentRepository.findByUniversityId(universityId);
        if (student != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        studentRepository.save(new Student(universityId));
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    /* Integration Functions */

    // GET

    @GetMapping(value = "/getStudent/{universityId}")
    public ResponseEntity<Student>  getStudentByUniversityId(@PathVariable String universityId) {
        return ResponseEntity.ok(studentRepository.findByUniversityId(universityId));
    }

    @GetMapping("/searchExamByContent/")
    public ResponseEntity<?> searchExamByContent(@RequestBody Exam exam) {
        Exam examDB = examRepository.findByContent(exam.getDescription(),
                exam.getDate(), exam.getTime(), exam.getLocation());

        if (examDB == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam is not present");

        return ResponseEntity.ok(examDB);
    }

    @DeleteMapping("/deleteStudent/{studentId}")
    public ResponseEntity deleteStudent(@PathVariable Long studentId) {
        Optional<Student> studentToDelete = studentRepository.findById(studentId);
        if (!studentToDelete.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam is not present");

        studentRepository.delete(studentToDelete.get());
        return ResponseEntity.status(HttpStatus.OK).body("Student deleted correctly");
    }

}
