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

    void deleteByTicketID(Integer ticketID);

    ArrayList<Ticket> findByUser_UsernameOrderByTicketDateAsc(String username);

    int countByCategoriesId(Integer categoryID);

    int countByPriorityId(Integer priorityID);

    int countByVersion_SoftwareId(Long softwareID);

    int countByStatus_StatusID(Integer statusID);

    @Query("Select t From Ticket t WHERE (lower(t.ticketTitle) like lower(concat('%',:fraza,'%')) or (lower(t.ticketDescription) like lower(concat('%',:fraza,'%'))) )")
    ArrayList<Ticket> searchByPhrase(@Param("fraza") String phrase);

    @Query("select t From Ticket t where t.ticketDate BETWEEN :date1 and :date2")
    ArrayList<Ticket> searchByDate(@Param("date1")LocalDate date1, @Param("date2")LocalDate date2);

    @Query("select t From Ticket t where t.version.software.id = ?1")
    ArrayList<Ticket> searchBySoftware(Long id);

//    @Query("select t From Ticket t where t.status.statusID = ?1")
    ArrayList<Ticket> searchByStatus(Integer statusID);

    @Query("select t From Ticket t where t.priority.id = ?1")
    ArrayList<Ticket> searchByPriority(Integer priorityID);

    @Query("select t From Ticket t WHERE concat(t.version.versionYear,'.',t.version.versionMonth,'.',t.version.versionNumber) like %?1% ")
    ArrayList<Ticket> searchByVersion(String toString);

    ArrayList<Ticket> findDistinctByCategoriesIn(Set<Category> categories);

    @Query("SELECT t FROM Ticket t WHERE SIZE(t.ticketReplies) BETWEEN ?1 and ?2")
    ArrayList<Ticket> searchByReplyNumber(int number1, int number2);

    void deleteByUserId(Integer id);

}
