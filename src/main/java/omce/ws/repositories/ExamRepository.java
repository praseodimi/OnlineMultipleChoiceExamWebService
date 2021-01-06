package omce.ws.repositories;

import omce.ws.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExamRepository extends CrudRepository<Exam, Long>, JpaRepository<Exam, Long>, JpaSpecificationExecutor<Exam> {

    @Query("SELECT e FROM Exam e where lower(e.description) like lower(concat('%', concat(:description, '%')))")
    List<Exam> findByDescription(String description);

    @Query("SELECT e FROM Exam e where " +
            "e.description = :description and " +
            "e.date = :date and " +
            "e.time = :time and " +
            "e.location = :location")
    Exam findByContent(String description, String date, String time, String location);
}

