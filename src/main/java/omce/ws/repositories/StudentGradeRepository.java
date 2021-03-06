package omce.ws.repositories;

import omce.ws.entities.StudentGrade;
import omce.ws.utils.StudentGradeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface StudentGradeRepository extends CrudRepository<StudentGrade, StudentGradeKey>, JpaRepository<StudentGrade, StudentGradeKey>, JpaSpecificationExecutor<StudentGrade> {

    @Query("SELECT sg FROM StudentGrade sg where sg.id.universityId = :universityId")
    Optional<List<StudentGrade>> findByUniversityId(String universityId);

    @Query("SELECT sg FROM StudentGrade sg where sg.id.examId = :examId")
    Optional<List<StudentGrade>> findByExamId(Long examId);
}
