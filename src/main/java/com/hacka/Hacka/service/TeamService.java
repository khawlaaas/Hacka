package com.hacka.Hacka.service;

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

    // ---------- lecture ----------
    public Hackathon getHackathon(Long hid) {
        return hackathonRepository.findById(hid)
                .orElseThrow(() -> new BusinessException("Hackathon introuvable."));
    }
    public List<Team> teamsOf(Long hid) { return teamRepository.findByHackathonIdOrderByCreatedAtDesc(hid); }
    public Optional<Team> findTeamOf(Long userId, Long hid) {
        return teamMemberRepository.findByTeam_Hackathon_IdAndUser_Id(hid, userId).map(TeamMember::getTeam);
    }
    public List<User> teamFinder(Long hid) {
        return registrationRepository.findUnteamedParticipants(hid, RegistrationStatus.CONFIRMED);
    }
    public boolean isLeader(Team team, Long userId) {
        return team.getMembers().stream()
                .anyMatch(m -> m.getRole() == TeamRole.LEADER && m.getUser().getId().equals(userId));
    }
    public List<TeamRequest> invitationsFor(Long userId) {
        return requestRepository.findByUserIdAndTypeAndStatus(userId, RequestType.INVITATION, RequestStatus.PENDING);
    }
    public List<TeamRequest> applicationsFor(Long leaderId) {
        return requestRepository.findForLeader(leaderId, TeamRole.LEADER, RequestType.APPLICATION, RequestStatus.PENDING);
    }

    // ---------- règles communes ----------
    private void requireRegistered(Long userId, Long hid) {
        boolean ok = registrationRepository.findByUserIdAndHackathonId(userId, hid)
                .map(r -> r.getStatus() == RegistrationStatus.CONFIRMED).orElse(false);
        if (!ok) throw new BusinessException("Il faut d'abord être inscrit à ce hackathon.");
    }
    private void requireNoTeam(Long userId, Long hid) {
        if (teamMemberRepository.existsByTeam_Hackathon_IdAndUser_Id(hid, userId))
            throw new BusinessException("Ce participant a déjà une équipe pour ce hackathon.");
    }
    private Team getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("Équipe introuvable."));
    }

    // ---------- actions ----------
    @Transactional
    public Team createTeam(Long userId, Long hid, TeamForm form) {
        Hackathon h = getHackathon(hid);
        User user = userRepository.findById(userId).orElseThrow();
        requireRegistered(userId, hid);
        requireNoTeam(userId, hid);

        Team team = new Team();
        team.setName(form.getName());
        team.setDescription(form.getDescription());
        team.setLookingForSkills(form.getLookingForSkills());
        team.setMaxSize(form.getMaxSize());
        team.setHackathon(h);
        team.getMembers().add(new TeamMember(team, user, TeamRole.LEADER));
        return teamRepository.save(team);
    }

    @Transactional
    public void apply(Long userId, Long teamId, String message) {          // candidature
        Team team = getTeam(teamId);
        Long hid = team.getHackathon().getId();
        requireRegistered(userId, hid);
        requireNoTeam(userId, hid);
        if (team.isFull()) throw new BusinessException("Cette équipe est complète.");
        if (requestRepository.existsByTeamIdAndUserIdAndStatus(teamId, userId, RequestStatus.PENDING))
            throw new BusinessException("Une demande est déjà en attente pour cette équipe.");
        saveRequest(team, userRepository.findById(userId).orElseThrow(), RequestType.APPLICATION, message);
    }

    @Transactional
    public void invite(Long leaderId, Long teamId, Long targetUserId) {    // invitation
        Team team = getTeam(teamId);
        Long hid = team.getHackathon().getId();
        if (!isLeader(team, leaderId)) throw new BusinessException("Seul le chef d'équipe peut inviter.");
        if (team.isFull()) throw new BusinessException("Votre équipe est complète.");
        requireRegistered(targetUserId, hid);
        requireNoTeam(targetUserId, hid);
        if (requestRepository.existsByTeamIdAndUserIdAndStatus(teamId, targetUserId, RequestStatus.PENDING))
            throw new BusinessException("Une invitation est déjà en attente.");
        saveRequest(team, userRepository.findById(targetUserId).orElseThrow(), RequestType.INVITATION, null);
    }

    private void saveRequest(Team team, User user, RequestType type, String message) {
        TeamRequest r = new TeamRequest();
        r.setTeam(team); r.setUser(user); r.setType(type); r.setMessage(message);
        requestRepository.save(r);
    }

    @Transactional
    public void respond(Long requestId, Long currentUserId, boolean accept) {
        TeamRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("Demande introuvable."));
        if (req.getStatus() != RequestStatus.PENDING)
            throw new BusinessException("Cette demande a déjà été traitée.");

        Team team = req.getTeam();
        // candidature → le chef décide ; invitation → c'est l'invité qui décide
        boolean allowed = req.getType() == RequestType.APPLICATION
                ? isLeader(team, currentUserId)
                : req.getUser().getId().equals(currentUserId);
        if (!allowed) throw new BusinessException("Action non autorisée.");

        if (!accept) { req.setStatus(RequestStatus.REJECTED); return; }

        if (team.isFull()) throw new BusinessException("L'équipe est complète.");
        requireNoTeam(req.getUser().getId(), team.getHackathon().getId());
        team.getMembers().add(new TeamMember(team, req.getUser(), TeamRole.MEMBER));
        req.setStatus(RequestStatus.ACCEPTED);
    }

    @Transactional
    public void leave(Long userId, Long teamId) {
        Team team = getTeam(teamId);
        TeamMember me = team.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(userId)).findFirst()
                .orElseThrow(() -> new BusinessException("Vous n'êtes pas membre de cette équipe."));
        team.getMembers().remove(me);                       // orphanRemoval supprime la ligne
        if (team.getMembers().isEmpty()) { teamRepository.delete(team); return; }
        if (me.getRole() == TeamRole.LEADER) {              // le plus ancien devient chef
            team.getMembers().stream().min(Comparator.comparing(TeamMember::getJoinedAt))
                    .ifPresent(m -> m.setRole(TeamRole.LEADER));
        }
    }
}