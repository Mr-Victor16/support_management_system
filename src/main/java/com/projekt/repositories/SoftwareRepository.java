package com.projekt.repositories;

import com.projekt.models.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {

    @Query("Select s FROM Software s where (lower(s.softwareName) like lower(concat('%',?1,'%')) or (lower(s.softwareDescription) like lower(concat('%',?1,'%'))))")
    ArrayList<Software> searchSoftwareByNameDescription(String phrase);
}
