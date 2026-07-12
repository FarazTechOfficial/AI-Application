package com.aitutor.app.service;

import com.aitutor.app.dto.StudentCreateRequest;
import com.aitutor.app.dto.StudentDto;
import com.aitutor.app.entity.Student;
import com.aitutor.app.mapper.Mapper;
import com.aitutor.app.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final Mapper mapper;

    public StudentDto createStudent(StudentCreateRequest request) {
        Student saved = studentRepository.save(mapper.toEntity(request));
        return mapper.toDto(saved);
    }

    public StudentDto getStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
        return mapper.toDto(student);
    }

    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream().map(mapper::toDto).toList();
    }
}
