package com.hacka.Hacka.service;

import com.hacka.Hacka.repository.*;

import com.hacka.Hacka.dto.TeamForm;
import com.hacka.Hacka.entity.*;
import com.hacka.Hacka.exception.BusinessException;
import com.hacka.Hacka.repository.RegistrationRepository;
import com.hacka.Hacka.repository.TeamMemberRepository;
import com.hacka.Hacka.repository.TeamRepository;
import com.hacka.Hacka.repository.TeamRequestRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRequestRepository requestRepository;
    private final RegistrationRepository registrationRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final BadgeService badgeService;

    // lecture

    public Hackathon getHackathon(Long hackathonId) {
        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
    }

    public List<Team> teamsOf(Long hackathonId) {
        return teamRepository.findByHackathonIdOrderByCreatedAtDesc(hackathonId);
    }

    public Optional<Team> findTeamOf(Long userId, Long hackathonId) {
        return teamMemberRepository.findByTeam_Hackathon_IdAndUser_Id(hackathonId, userId)
                .map(TeamMember::getTeam);
    }

    /** Team Finder : participants inscrits qui n'ont pas encore d'équipe. */
    public List<User> teamFinder(Long hackathonId) {
        return registrationRepository.findUnteamedParticipants(hackathonId, RegistrationStatus.CONFIRMED);
    }

    public boolean isLeader(Team team, Long userId) {
        return team.getMembers().stream()
                .anyMatch(m -> m.getRole() == TeamRole.LEADER && m.getUser().getId().equals(userId));
    }

    public List<TeamRequest> invitationsFor(Long userId) {
        return requestRepository.findByUserIdAndTypeAndStatus(
                userId, RequestType.INVITATION, RequestStatus.PENDING);
    }

    public List<TeamRequest> applicationsFor(Long leaderId) {
        return requestRepository.findForLeader(
                leaderId, TeamRole.LEADER, RequestType.APPLICATION, RequestStatus.PENDING);
    }

    // règles communes

    private void requireRegistered(Long userId, Long hackathonId) {
        boolean ok = registrationRepository.findByUserIdAndHackathonId(userId, hackathonId)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
                .orElse(false);
        if (!ok) {
            throw new BusinessException("Il faut d'abord être inscrit à ce hackathon.");
        }
    }

    private void requireNoTeam(Long userId, Long hackathonId) {
        if (teamMemberRepository.existsByTeam_Hackathon_IdAndUser_Id(hackathonId, userId)) {
            throw new BusinessException("Ce participant a déjà une équipe pour ce hackathon.");
        }
    }

    private Team getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("Équipe introuvable."));
    }

    // ------------------------------------------------------------------- actions

    @Transactional
    public Team createTeam(Long userId, Long hackathonId, TeamForm form) {
        Hackathon hackathon = getHackathon(hackathonId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        requireRegistered(userId, hackathonId);
        requireNoTeam(userId, hackathonId);

        Team team = new Team();
        team.setName(form.getName());
        team.setDescription(form.getDescription());
        team.setLookingForSkills(form.getLookingForSkills());
        team.setMaxSize(form.getMaxSize());
        team.setHackathon(hackathon);
        team.getMembers().add(new TeamMember(team, user, TeamRole.LEADER));
        Team saved = teamRepository.save(team);
        badgeService.evaluate(userId);
        return saved;
    }

    /** Candidature d'un participant à une équipe. */
    @Transactional
    public Team apply(Long userId, Long teamId, String message) {
        Team team = getTeam(teamId);
        Long hackathonId = team.getHackathon().getId();
        requireRegistered(userId, hackathonId);
        requireNoTeam(userId, hackathonId);
        if (team.isFull()) {
            throw new BusinessException("Cette équipe est complète.");
        }
        if (requestRepository.existsByTeamIdAndUserIdAndStatus(teamId, userId, RequestStatus.PENDING)) {
            throw new BusinessException("Une demande est déjà en attente pour cette équipe.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        saveRequest(team, user, RequestType.APPLICATION, message);
        return team;
    }

    /** Invitation d'un participant par le chef d'équipe. */
    @Transactional
    public Team invite(Long leaderId, Long teamId, Long targetUserId) {
        Team team = getTeam(teamId);
        Long hackathonId = team.getHackathon().getId();
        if (!isLeader(team, leaderId)) {
            throw new BusinessException("Seul le chef d'équipe peut inviter.");
        }
        if (team.isFull()) {
            throw new BusinessException("Votre équipe est complète.");
        }
        requireRegistered(targetUserId, hackathonId);
        requireNoTeam(targetUserId, hackathonId);
        if (requestRepository.existsByTeamIdAndUserIdAndStatus(teamId, targetUserId, RequestStatus.PENDING)) {
            throw new BusinessException("Une invitation est déjà en attente pour ce participant.");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        saveRequest(team, target, RequestType.INVITATION, null);
        return team;
    }

    private void saveRequest(Team team, User user, RequestType type, String message) {
        TeamRequest request = new TeamRequest();
        request.setTeam(team);
        request.setUser(user);
        request.setType(type);
        request.setMessage(message);
        requestRepository.save(request);
    }

    /** Accepter ou refuser une demande. Candidature : décide le chef. Invitation : décide l'invité. */
    @Transactional
    public void respond(Long requestId, Long currentUserId, boolean accept) {
        TeamRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Demande introuvable."));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Cette demande a déjà été traitée.");
        }

        Team team = request.getTeam();
        boolean allowed = request.getType() == RequestType.APPLICATION
                ? isLeader(team, currentUserId)
                : request.getUser().getId().equals(currentUserId);
        if (!allowed) {
            throw new BusinessException("Action non autorisée.");
        }

        if (!accept) {
            request.setStatus(RequestStatus.REJECTED);
            return;
        }
        if (team.isFull()) {
            throw new BusinessException("L'équipe est complète.");
        }
        Long newMemberId = request.getUser().getId();
        requireNoTeam(newMemberId, team.getHackathon().getId());

        team.getMembers().add(new TeamMember(team, request.getUser(), TeamRole.MEMBER));
        request.setStatus(RequestStatus.ACCEPTED);
        badgeService.evaluate(newMemberId);
    }

    @Transactional
    public void leave(Long userId, Long teamId) {
        Team team = getTeam(teamId);
        TeamMember me = team.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Vous n'êtes pas membre de cette équipe."));

        team.getMembers().remove(me);            // orphanRemoval supprime la ligne team_members
        if (team.getMembers().isEmpty()) {
            teamRepository.delete(team);         // plus personne : on supprime l'équipe
            return;
        }
        if (me.getRole() == TeamRole.LEADER) {   // le membre le plus ancien devient chef
            team.getMembers().stream()
                    .min(Comparator.comparing(TeamMember::getJoinedAt))
                    .ifPresent(m -> m.setRole(TeamRole.LEADER));
        }
    }
}
