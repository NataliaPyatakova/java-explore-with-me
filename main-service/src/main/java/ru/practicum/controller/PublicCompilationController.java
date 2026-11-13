package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.ParamCompilationFindAll;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> findAll(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                        @RequestParam(value = "from", defaultValue = "0") Integer from,
                                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("PublicCompilationController findAll pinned {} from {} size {}", pinned, from, size);
        //для необязательного pinned не описано дефолтное значение - считаем что дефолтно ничего не выбрано - берем все
        ParamCompilationFindAll params = new ParamCompilationFindAll(pinned, from, size);
        return compilationService.findAll(params);
    }

    @GetMapping("/{compId}")
    public CompilationDto findById(@PathVariable @Positive Long compId) {
        log.info("PublicCompilationController findById compId {}", compId);
        return compilationService.findById(compId);
    }
}