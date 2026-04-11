package edu.cit.yabao.supportstack.repository;

import edu.cit.yabao.supportstack.model.Ticket;
import edu.cit.yabao.supportstack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
        @Query("""
                        select t
                        from Ticket t
                        where t.createdByUser = :createdByUser
                            and (t.isDeleted = false or t.isDeleted is null)
                        order by t.createdAt desc
                        """)
        List<Ticket> findByCreatedByUserAndNotDeletedOrderByCreatedAtDesc(User createdByUser);

    List<Ticket> findByIsDeletedFalseOrderByCreatedAtDesc();

    List<Ticket> findByIsDeletedTrueOrderByDeletedAtDesc();

    Optional<Ticket> findByIdAndCreatedByUser(Long id, User createdByUser);
}