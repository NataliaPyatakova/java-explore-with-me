package ru.practicum.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ParamCompilationFindAll;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto save(NewCompilationDto newDto);

    CompilationDto update(Long compId, UpdateCompilationRequest updateDto);

    void delete(Long compId);

    CompilationDto findById(Long compId);

    List<CompilationDto> findAll(ParamCompilationFindAll params);
}
