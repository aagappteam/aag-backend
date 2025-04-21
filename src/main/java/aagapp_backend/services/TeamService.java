package aagapp_backend.services;

import aagapp_backend.dto.TeamDTO;
import aagapp_backend.entity.Team;
import aagapp_backend.entity.league.League;
import aagapp_backend.repository.team.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }


    @Transactional
    public Team getTeamById(Long teamId) {
        return teamRepository.getTeamById(teamId);
    }

    @Transactional
    public TeamDTO createTeam(Team team) {
        if (team.getName() == null || team.getName().isEmpty()) {
            throw new IllegalArgumentException("Team name must not be null or empty");
        }

        if (team.getLeague() != null && team.getLeague().getId() != null) {
            League league = team.getLeague();
            Long leagueId = league.getId();

            // Check if a team with the same name exists in the same league
            boolean teamExists = teamRepository.findByLeagueId(leagueId).stream()
                    .anyMatch(existingTeam -> existingTeam.getName().equalsIgnoreCase(team.getName()));

            if (teamExists) {
                throw new IllegalArgumentException("Team with this name already exists in the league");
            }
        } else {
            throw new IllegalArgumentException("League ID must be provided");
        }

        team.setCreatedDate(ZonedDateTime.now());
        teamRepository.save(team);

        return mapToDTO(team);
    }

    @Transactional
    public TeamDTO updateTeam(Long teamId, Team teamDetails) {
        Team team = teamRepository.getTeamById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        if (teamDetails.getName() != null && !teamDetails.getName().isEmpty()) {
            team.setName(teamDetails.getName());
        }

        if (teamDetails.getLeague() != null && teamDetails.getLeague().getId() != null) {
            League league = teamDetails.getLeague();
            team.setLeague(league);
        }

        team.setUpdatedDate(ZonedDateTime.now());
        teamRepository.save(team);

        return mapToDTO(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.getTeamById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }
        teamRepository.deleteTeam(teamId);
    }

    private TeamDTO mapToDTO(Team team) {
        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getLeague() != null ? team.getLeague().getId() : null,
                team.getLeague() != null ? team.getLeague().getName() : null,
                team.getCreatedDate().toLocalDateTime()
        );
    }
}
