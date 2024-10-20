package com.projekt.services;

import com.projekt.models.Knowledge;
import com.projekt.models.Software;
import com.projekt.payload.request.update.UpdateKnowledgeRequest;
import com.projekt.repositories.KnowledgeRepository;
import com.projekt.repositories.SoftwareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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

    //boolean findDuplicate(String title, Long softwareID);
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

    //boolean findDuplicate(String title, Long softwareID);
    //Tests that the method returns false when no knowledge entry with the given title exists
    @Test
    void shouldReturnFalse_whenKnowledgeWithTitleDoesNotExist() {
        String knowledgeTitle = "Non-existing Knowledge";
        Long softwareID = 1L;

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(null);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    //boolean findDuplicate(String title, Long softwareID);
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

    //void update(UpdateKnowledgeRequest request);
    //Tests the update method to ensure the knowledge is correctly updated based on the data from the UpdateKnowledgeRequest object.
    @Test
    void testUpdateKnowledge() {
        Long knowledgeID = 1L;
        Long softwareID = 2L;

        UpdateKnowledgeRequest request = new UpdateKnowledgeRequest(knowledgeID, "Updated Title", "Updated Content", softwareID);

        Software software = new Software(2L, "SoftwareName", "SoftwareDescription");
        Knowledge knowledge = new Knowledge(knowledgeID, "Title", "Content", new Software());

        when(knowledgeRepository.findById(knowledgeID)).thenReturn(Optional.of(knowledge));
        when(softwareRepository.findById(softwareID)).thenReturn(Optional.of(software));

        knowledgeBaseService.update(request);

        assertEquals("Updated Title", knowledge.getTitle());
        assertEquals("Updated Content", knowledge.getContent());
        assertEquals(softwareID, knowledge.getSoftware().getId());
        assertEquals("SoftwareName", knowledge.getSoftware().getName());

        verify(knowledgeRepository).save(knowledge);
    }
}
