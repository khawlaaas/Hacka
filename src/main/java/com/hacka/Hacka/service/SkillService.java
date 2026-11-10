package com.hacka.Hacka.service;

import com.hacka.Hacka.entity.Skill;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<Skill> findAll() {
        return skillRepository.findAll(Sort.by("name"));
    }

    // Les deux méthodes suivantes serviront à l'admin (/admin/skills), plus tard.
    @Transactional
    public void create(String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty())
            throw new BusinessException("Le nom de la compétence est obligatoire.");
        if (skillRepository.existsByNameIgnoreCase(n))
            throw new BusinessException("Cette compétence existe déjà.");
        skillRepository.save(new Skill(n));
    }

    @Transactional
    public void delete(Long id) {
        try {
            skillRepository.deleteById(id);
            skillRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Cette compétence est utilisée par des utilisateurs : suppression impossible.");
        }
    }
}