package com.projekt.repositories;

import com.projekt.models.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Integer> {

    int countBySoftware_SoftwareID(Long softwareID);

    @Query("Select k From Knowledge k Where ((lower(k.knowledgeTitle) like lower(concat('%',?1,'%'))) or (lower(k.knowledgeContent) like lower(concat('%',?1,'%'))) )")
    ArrayList<Knowledge> searchKnowledgeByTitleContent(String phrase);

    @Query("select k From Knowledge k WHERE k.software.softwareID = :softwareID")
    ArrayList<Knowledge> searchKnowledgeBySoftware(@Param("softwareID") Long softwareID);

    @Query("select k From Knowledge k where k.knowledgeDate BETWEEN :date1 and :date2")
    ArrayList<Knowledge> searchKnowledgeByDate(@Param("date1")LocalDate date1, @Param("date2")LocalDate date2);

}
