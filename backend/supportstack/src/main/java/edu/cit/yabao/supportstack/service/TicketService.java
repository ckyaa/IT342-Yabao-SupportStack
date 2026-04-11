package edu.cit.yabao.supportstack.service;

import edu.cit.yabao.supportstack.dto.CreateTicketRequest;
import edu.cit.yabao.supportstack.dto.TicketResponse;
import edu.cit.yabao.supportstack.exception.InvalidCredentialsException;
import edu.cit.yabao.supportstack.exception.TicketNotFoundException;
import edu.cit.yabao.supportstack.model.ChangeType;
import edu.cit.yabao.supportstack.model.Department;
import edu.cit.yabao.supportstack.model.Ticket;
import edu.cit.yabao.supportstack.model.TicketStatus;
import edu.cit.yabao.supportstack.model.User;
import edu.cit.yabao.supportstack.repository.DepartmentRepository;
import edu.cit.yabao.supportstack.repository.TicketHistoryRepository;
import edu.cit.yabao.supportstack.repository.TicketRepository;
import edu.cit.yabao.supportstack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         DepartmentRepository departmentRepository, TicketHistoryRepository ticketHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    public TicketResponse createTicket(CreateTicketRequest request, String principal) {
        User createdByUser = resolveUser(principal);
        Department department = departmentRepository.findByCodeIgnoreCase(request.department())
                .orElseThrow(() -> new InvalidCredentialsException("Department not found"));

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setCreatedByUser(createdByUser);
        ticket.setOwner(createdByUser);
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setDepartment(department);
        ticket.setDepartmentLegacy(department.getCode());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setIsDeleted(false);

        Ticket savedTicket = ticketRepository.save(ticket);

        try {
            createHistoryEntry(savedTicket, createdByUser, ChangeType.CREATED, null, null, null);
        } catch (Exception ex) {
            log.warn("Ticket created but history logging failed for ticketId={}", savedTicket.getId(), ex);
        }

        return toResponse(savedTicket);
    }

    public List<TicketResponse> getMyTickets(String principal) {
        User createdByUser = resolveUser(principal);
        return ticketRepository.findByCreatedByUserAndNotDeletedOrderByCreatedAtDesc(createdByUser)
                .stream()
                .map(this::toResponseSafely)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public TicketResponse getMyTicket(Long ticketId, String principal) {
        User createdByUser = resolveUser(principal);
        Ticket ticket = ticketRepository.findByIdAndCreatedByUser(ticketId, createdByUser)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        return toResponse(ticket);
    }

    private User resolveUser(String principal) {
        return userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(principal, principal)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    private String generateTicketNumber() {
        // Format: TKT-XXXXXXXX (TKT- + 8 random alphanumeric)
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createHistoryEntry(Ticket ticket, User changedByUser, ChangeType changeType,
                                    String fieldName, String oldValue, String newValue) {
        var history = new edu.cit.yabao.supportstack.model.TicketHistory();
        history.setTicket(ticket);
        history.setChangedByUser(changedByUser);
        history.setChangeType(changeType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        ticketHistoryRepository.save(history);
    }

    private TicketResponse toResponse(Ticket ticket) {
        String departmentCode = ticket.getDepartment() != null ? ticket.getDepartment().getCode() : "UNASSIGNED";
        String status = ticket.getStatus() != null ? ticket.getStatus().name() : TicketStatus.OPEN.name();
        String ticketNumber = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "TKT-LEGACY-" + ticket.getId();

        return new TicketResponse(
                ticket.getId(),
            ticketNumber,
                ticket.getTitle(),
                ticket.getDescription(),
            departmentCode,
            status,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private TicketResponse toResponseSafely(Ticket ticket) {
        try {
            return toResponse(ticket);
        } catch (Exception ex) {
            log.warn("Skipping unreadable ticket record id={}", ticket != null ? ticket.getId() : null, ex);
            return null;
        }
    }
}