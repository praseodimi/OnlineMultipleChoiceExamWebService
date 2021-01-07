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
import org.springframework.dao.DataIntegrityViolationException;
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
    public ResponseEntity createExam(@RequestBody Exam exam) {

        Exam examDB = examRepository.findByContent(exam.getDescription(),
                exam.getDate(), exam.getTime(), exam.getLocation());
        if (examDB != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Exam is already created");

        try{
            Exam e = examRepository.save(exam);
            return ResponseEntity.status(HttpStatus.CREATED).body("Exam created");
        } catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error creating the exam");
        }

    }

    // Modify exam’s description.
    // PUT
    @PutMapping("/updateExamDescription/{examId}")
    public ResponseEntity updateExamDescription(@PathVariable Long examId, @RequestBody String description) {
        Optional<Exam> examToEdit = examRepository.findById(examId);
        if (!examToEdit.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");
        examToEdit.get().setDescription(description);
        examRepository.save(examToEdit.get());
        return ResponseEntity.status(HttpStatus.OK).body("Exam updated correctly");
    }

    // Delete exam (if it has no grades).
    // DELETE
    @DeleteMapping("/deleteExam/{examId}")
    public ResponseEntity deleteExam(@PathVariable Long examId) {
        Optional<Exam> examToDelete = examRepository.findById(examId);
        if (!examToDelete.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        Optional<List<StudentGrade>> studentGrades = studentGradeRepository.findByExamId(examId);
        if (!studentGrades.isPresent()){
            examRepository.delete(examToDelete.get());
            return ResponseEntity.status(HttpStatus.OK).body("Exam deleted correctly");
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body("Exam has grades");
    }


    // Search exam content using the key or search them using textual description (full/partial search).
    // GET
    @GetMapping(value = "/searchExamById/{id}")
    public ResponseEntity searchExamById(@PathVariable Long id) {
        Optional<Exam> exam = examRepository.findById(id);
        if (!exam.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        return ResponseEntity.ok(exam.get());
    }

    @GetMapping("/searchExamByDescription/{description}")
    public ResponseEntity searchExamByDescription(@PathVariable String description) {
        List<Exam> exams = examRepository.findByDescription(description);

        if (exams.size() == 0)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        return ResponseEntity.ok(exams);
    }


    // Download exams information. By Id or listing all the contents in the system.
    // GET

    @GetMapping("/downloadExam/{examId}")
    public ResponseEntity downloadExamById(@PathVariable Long examId) {

        Optional<Exam> exam = examRepository.findById(examId);
        if (!exam.isPresent())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(exam.get());
            byte[] examBytes = json.getBytes();
            String fileName = "Exam" + examId + ".json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading the exam");
        }

    }

    @GetMapping("/downloadExams")
    public ResponseEntity downloadExams() {

        List<Exam> exams = examRepository.findAll();
        if (exams.size() == 0)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(exams);
            byte[] examBytes = json.getBytes();
            String fileName = "Exams.json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading the exams");
        }
    }

    /* Advanced Functions */

    // Upload grades to an exam.
    // POST
    @PostMapping("/uploadGrades")
    public ResponseEntity uploadGrades(@RequestBody List<StudentGradeInfo> studentGrades) {

        if (studentGrades.size() == 0)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Request body is empty");

        try{
            for (StudentGradeInfo sg: studentGrades) {
                StudentGrade studentGrade = new StudentGrade(
                        sg.getUniversityId(),
                        examRepository.findById(sg.getExamId()).get(),
                        sg.getGrade());
                studentGradeRepository.save(studentGrade);
            }
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Exam does not exist");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Student grade created");
    }

    // Download student’s grades.
    // GET
    @GetMapping("/downloadStudentGrades/{universityId}")
    public ResponseEntity downloadStudentGrades(@PathVariable String universityId) {

        List<StudentGrade> studentGrades = studentGradeRepository.findByUniversityId(universityId);
        if (studentGrades.size() == 0)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Student does not exist");

        ArrayList<StudentGradeInfo> studentGradesInfo = new ArrayList<>();
        for (StudentGrade sg : studentGrades) {
            StudentGradeInfo studentGradeInfo = new StudentGradeInfo(universityId,
                    sg.getExam().getExamId(), sg.getGrade());
            studentGradesInfo.add(studentGradeInfo);
        }

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(studentGradesInfo);
            byte[] examBytes = json.getBytes();
            String fileName = universityId + "grades.json";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(examBytes.length).body(examBytes);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error downloading grades");
        }

    }

    // Manage student’s access (by ID).
    // POST
    @PostMapping("/createStudent")
    public ResponseEntity createStudent(@RequestBody Student student) {

        Student studentDB = studentRepository.findByUniversityId(student.getUniversityId());
        if (studentDB != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Student is already created");

        try{
            studentRepository.save(new Student(student.getUniversityId()));
            return ResponseEntity.status(HttpStatus.CREATED).body("Student created");
        } catch (DataIntegrityViolationException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Incorrect request body");
        }
    }

    /* Integration Functions */

    // GET

    @GetMapping("/searchExamByContent")
    @ResponseBody
    public ResponseEntity getExamByContent(@RequestParam List<String> content) {
        Exam exam = new Exam(content.get(0), content.get(1), content.get(2), content.get(3));
        Exam examDB = examRepository.findByContent(exam.getDescription(),
                exam.getDate(), exam.getTime(), exam.getLocation());

        if (examDB == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Exam does not exist");

        return ResponseEntity.ok(examDB);
    }

    @GetMapping(value = "/getStudent/{universityId}")
    public ResponseEntity<Student>  getStudentByUniversityId(@PathVariable String universityId) {
        return ResponseEntity.ok(studentRepository.findByUniversityId(universityId));
    }

    @DeleteMapping("/deleteStudent/{universityId}")
    public ResponseEntity deleteStudent(@PathVariable String universityId) {
        Student studentToDelete = studentRepository.findByUniversityId(universityId);
        if (studentToDelete == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Student does not exist");
        studentRepository.delete(studentToDelete);
        return ResponseEntity.status(HttpStatus.OK).body("Student deleted correctly");
    }

}
