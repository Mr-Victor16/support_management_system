package com.projekt.repositories;

import com.projekt.models.Category;
import com.projekt.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    ArrayList<Ticket> findByUser_UsernameOrderByDateAsc(String username);

    int countByCategoriesId(Integer categoryID);

    int countByPriorityId(Integer priorityID);

    long countBySoftwareId(Long id);

    int countByStatus_Id(Integer statusID);

    @Query("Select t From Ticket t WHERE (lower(t.title) like lower(concat('%',:fraza,'%')) or (lower(t.description) like lower(concat('%',:fraza,'%'))) )")
    ArrayList<Ticket> searchByPhrase(@Param("fraza") String phrase);

    @Query("select t From Ticket t where t.date BETWEEN :date1 and :date2")
    ArrayList<Ticket> searchByDate(@Param("date1") LocalDate date1, @Param("date2") LocalDate date2);

    @Query("select t From Ticket t where t.software.id = ?1")
    ArrayList<Ticket> searchBySoftware(Long id);

    ArrayList<Ticket> searchByStatus(Integer statusID);

    @Query("select t From Ticket t where t.priority.id = ?1")
    ArrayList<Ticket> searchByPriority(Integer priorityID);

    @Query("select t From Ticket t WHERE t.version like %?1% ")
    ArrayList<Ticket> searchByVersion(String toString);

    ArrayList<Ticket> findDistinctByCategoriesIn(Set<Category> categories);

    @Query("SELECT t FROM Ticket t WHERE SIZE(t.ticketReplies) BETWEEN ?1 and ?2")
    ArrayList<Ticket> searchByReplyNumber(int number1, int number2);

    void deleteByUserId(Integer id);

}
