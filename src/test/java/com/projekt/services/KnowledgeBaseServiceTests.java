package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.payload.request.edit.EditKnowledgeRequest;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeBaseServiceTests {
    private KnowledgeBaseService knowledgeBaseService;
    private KnowledgeRepository knowledgeRepository;
    private SoftwareRepository softwareRepository;

    @BeforeEach
    public void setUp() {
        knowledgeRepository = mock(KnowledgeRepository.class);
        softwareRepository = mock(SoftwareRepository.class);

        knowledgeBaseService = new KnowledgeBaseServiceImpl(knowledgeRepository, softwareRepository);
    }

    //boolean findDuplicate(String knowledgeTitle, Long softwareID);
    //Tests that the method returns true when a knowledge entry with the same title and software ID exists
    @Test
    void shouldReturnTrue_whenKnowledgeWithSameTitleAndSoftwareIdExists() {
        String knowledgeTitle = "Existing Knowledge";
        Long softwareID = 1L;

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(softwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertTrue(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    //boolean findDuplicate(String knowledgeTitle, Long softwareID);
    //Tests that the method returns false when no knowledge entry with the given title exists
    @Test
    void shouldReturnFalse_whenKnowledgeWithTitleDoesNotExist() {
        String knowledgeTitle = "Non-existing Knowledge";
        Long softwareID = 1L;

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(null);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    //boolean findDuplicate(String knowledgeTitle, Long softwareID);
    //Tests that the method returns false when a knowledge entry with the same title but different software ID exists
    @Test
    void shouldReturnFalse_whenKnowledgeWithSameTitleButDifferentSoftwareIdExists() {
        String knowledgeTitle = "Existing Knowledge";
        Long softwareID = 1L;
        Long differentSoftwareID = 2L;

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(differentSoftwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    //void update(EditKnowledgeRequest knowledgeRequest);
    //Tests the update method to ensure the knowledge is correctly updated based on the data from the EditKnowledgeRequest object.
    @Test
    void shouldUpdateKnowledge() {
        Long knowledgeId = 1L;
        Long softwareId = 2L;

        EditKnowledgeRequest request = new EditKnowledgeRequest(knowledgeId, "Updated Title", "Updated Content", LocalDate.now(), softwareId);

        Software software = new Software(2L, "SoftwareName", "SoftwareDescription");
        Knowledge knowledge = new Knowledge(knowledgeId, "Title", "Content", LocalDate.of(2022,10,1), new Software());

        when(knowledgeRepository.getReferenceById(knowledgeId)).thenReturn(knowledge);
        when(softwareRepository.getReferenceById(softwareId)).thenReturn(software);

        knowledgeBaseService.update(request);

        assertEquals("Updated Title", knowledge.getTitle());
        assertEquals("Updated Content", knowledge.getContent());
        assertEquals(request.getDate(), knowledge.getDate());
        assertEquals(softwareId, knowledge.getSoftware().getId());

        verify(knowledgeRepository).save(knowledge);
    }
}
