package org.course.registration.service;

import lombok.RequiredArgsConstructor;
import org.course.registration.domain.Course;
import org.course.registration.domain.Enroll;
import org.course.registration.domain.Student;
import org.course.registration.exception.NotEnoughException;
import org.course.registration.repository.EnrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollService {
    private final EnrollRepository enrollRepository;
    private final CourseService courseService;
    private final StudentService studentService;

    @Transactional
    public void enrollCourse(int studentId, int courseId) {
        // 학생과 과목을 조회
        Student student = studentService.findStudentById(studentId);
        Course course = courseService.findCourseById(courseId);

        // 과목 수강 정원 체크
        if (course.getCount() >= course.getLimited()) {
            throw new NotEnoughException("정원이 다 찬 과목입니다.");
        }

        // 이미 수강 중인 과목인지 체크
        Optional<Enroll> existingEnroll = enrollRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existingEnroll.isPresent()) {
            throw new IllegalStateException("이미 수강 중인 과목입니다.");
        }

        // 수강 신청 진행
        Enroll newEnroll = new Enroll();
        newEnroll.setStudent(student);
        newEnroll.setCourse(course);
        enrollRepository.save(newEnroll);

        // 과목 수강 인원 증가
        course.setCount(course.getCount() + 1);
        courseService.updateCourse(course); // 변경된 course 엔티티를 업데이트
    }


    // 학생 ID로 수강신청한 과목 리스트 조회
    public List<Course> findEnrollmentsByStudentId(int studentId) {
        return enrollRepository.findCoursesByStudentId(studentId);
    }

    // 수강 취소
    @Transactional
    public void cancelEnrollment(int studentId, int courseId) {
        Course course = courseService.findCourseById(courseId);
        enrollRepository.deleteByStudentIdAndCourseId(studentId, courseId);
        // 수강 인원 감소
        int newCount = Math.max(0, course.getCount() - 1); // 0보다 밑으로 내려가는 거 방지
        course.setCount(newCount);
        courseService.updateCourse(course); // 변경된 course 엔티티를 업데이트
    }
}
