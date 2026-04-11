package edu.cit.yabao.supportstack.repository;

import edu.cit.yabao.supportstack.model.Ticket;
import edu.cit.yabao.supportstack.model.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
