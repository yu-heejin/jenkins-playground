package org.course.registration.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.course.registration.domain.Course;
import org.course.registration.domain.Student;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

    private final EntityManager em;

    public List<Course> findAll(){
        return em.createQuery("select c from Course c", Course.class)
                .getResultList();
    }
}
