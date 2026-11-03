package org.raflab.platforma_server.config;



import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.model.*;
import org.raflab.platforma_server.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ExamPeriodRepository examPeriodRepository;
    private final ExamTermRepository examTermRepository;
    private final GroupRepository groupRepository;
    private final ExamRegistrationRepository examRegistrationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User studentUser = new User();
            studentUser.setUsername("student1");
            studentUser.setPassword("password123");
            studentUser.setRole(Role.STUDENT);
            userRepository.save(studentUser);

            Student student = new Student();
            student.setFirstName("Petar");
            student.setLastName("Petrovic");
            student.setIndexNumber("RN1023");
            student.setUser(studentUser);
            studentRepository.save(student);

            Course course = new Course();
            course.setCode("DBS");
            course.setName("Database Systems");
            courseRepository.save(course);

            ExamPeriod examPeriod = new ExamPeriod();
            examPeriod.setName("January 2026");
            examPeriod.setCourse(course);
            examPeriodRepository.save(examPeriod);

            ExamTerm examTerm = new ExamTerm();
            examTerm.setStartTime(LocalDateTime.now().plusDays(1));
            examTerm.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));
            examTerm.setExamPeriod(examPeriod);
            examTermRepository.save(examTerm);

            Group group = new Group();
            group.setName("Group 1");
            group.setExamTerm(examTerm);
            groupRepository.save(group);

            ExamRegistration registration = new ExamRegistration();
            registration.setStudent(student);
            registration.setExamTerm(examTerm);
            registration.setGroup(group);
            examRegistrationRepository.save(registration);

            System.out.println("Data seeding completed successfully.");
        }
    }
}