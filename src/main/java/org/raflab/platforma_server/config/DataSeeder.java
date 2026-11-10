package org.raflab.platforma_server.config;



import lombok.RequiredArgsConstructor;
import org.raflab.platforma_server.model.*;
import org.raflab.platforma_server.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        /*
        userRepository.findByUsername("student1")
                .filter(user -> user.getPassword().equals("password123"))
                .ifPresent(user -> {
                    user.setPassword(passwordEncoder.encode("password123"));
                    userRepository.save(user);
                });

         */

        User studentUser1 = userRepository.findByUsername("student1").orElseGet(() -> {
            User user = new User();
            user.setUsername("student1");
            user.setRole(Role.STUDENT);
            user.setPassword(passwordEncoder.encode("password123"));
            return userRepository.save(user);
        });

        if (studentRepository.findByUser_Username("student1").isEmpty()) {
            Student student = new Student();
            student.setFirstName("Petar");
            student.setLastName("Petrovic");
            student.setIndexNumber("RN1023");
            student.setUser(studentUser1);
            studentRepository.save(student);
        }

        if (userRepository.findByUsername("teacher1").isEmpty()) {
            User teacherUser = new User();
            teacherUser.setUsername("teacher1");
            teacherUser.setPassword(passwordEncoder.encode("password123"));
            teacherUser.setRole(Role.TEACHER);
            userRepository.save(teacherUser);
        }

        User studentUser2 = userRepository.findByUsername("student2").orElseGet(() -> {
            User user = new User();
            user.setUsername("student2");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.STUDENT);
            return userRepository.save(user);
        });

        if (studentRepository.findByUser_Username("student2").isEmpty()) {
            Student student2 = new Student();
            student2.setFirstName("Test");
            student2.setLastName("Student");
            student2.setIndexNumber("TEST0002");
            student2.setUser(studentUser2);
            studentRepository.save(student2);
        }

        if (courseRepository.count() == 0) {
            // Create and save a student user
            Student student = studentRepository.findByUser_Username("student1").get();

            // Create and save a course, exam period, exam term, group, and exam registration
            Course course = new Course();
            course.setCode("DBS");
            course.setName("Database Systems");
            courseRepository.save(course);

            ExamPeriod examPeriod = new ExamPeriod();
            examPeriod.setName("January 2026");
            examPeriod.setCourse(course);
            examPeriodRepository.save(examPeriod);

            ExamTerm activeTerm = new ExamTerm();
            activeTerm.setStartTime(LocalDateTime.now().minusHours(1));;
            activeTerm.setEndTime(LocalDateTime.now().plusHours(2));
            activeTerm.setExamPeriod(examPeriod);
            examTermRepository.save(activeTerm);

            ExamTerm pastTerm = new ExamTerm();
            pastTerm.setStartTime(LocalDateTime.now().minusHours(5));;
            pastTerm.setEndTime(LocalDateTime.now().minusHours(2));
            pastTerm.setExamPeriod(examPeriod);
            examTermRepository.save(pastTerm);

            Group group = new Group();
            group.setName("Group 1");
            group.setExamTerm(activeTerm);
            groupRepository.save(group);

            Group expiredGroup = new Group();
            expiredGroup.setName("Group 2 (Expired)");
            expiredGroup.setExamTerm(pastTerm);
            groupRepository.save(expiredGroup);

            ExamRegistration registration = new ExamRegistration();
            registration.setStudent(student);
            registration.setExamTerm(activeTerm);
            registration.setGroup(group);
            examRegistrationRepository.save(registration);

            ExamRegistration expiredRegistration = new ExamRegistration();
            expiredRegistration.setStudent(student);
            expiredRegistration.setExamTerm(pastTerm);
            expiredRegistration.setGroup(expiredGroup);
            examRegistrationRepository.save(expiredRegistration);



            System.out.println("Data seeding completed successfully.");
        }
    }
}