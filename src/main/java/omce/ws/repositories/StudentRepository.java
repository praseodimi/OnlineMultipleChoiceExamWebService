package omce.ws.repositories;

import omce.ws.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface StudentRepository extends CrudRepository<Student, Long>, JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("SELECT s FROM Student s where s.universityId = :universityId")
    Student findByUniversityId(String universityId);

}
