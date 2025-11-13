package ru.yandex.practicum.filmorate.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaStorage;
    private final Validators validators;

    public List<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa findById(Integer mpaId) {
        validators.validateMpaExists(mpaId, getClass());
        return mpaStorage.findById(mpaId);
    }
}
