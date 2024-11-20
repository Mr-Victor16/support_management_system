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

    /**
     * Method: boolean findDuplicate(Long knowledgeID, String knowledgeTitle, Long softwareID)
     * Description: Knowledge has unique title. Test should return false.
     * Expected return: FALSE
     */
    @Test
    void findDuplicateWithKnowledgeId_knowledgeTitleIsUnique_shouldReturnFalse(){
        String knowledgeTitle = "Non-existing Knowledge";
        long softwareID = 1;
        long knowledgeID = 2;

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(null);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeID, knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(Long knowledgeID, String knowledgeTitle, Long softwareID)
     * Description: Knowledge with the same title exists, but has a different software ID.
     * Expected return: FALSE
     */
    @Test
    void findDuplicateWithKnowledgeId_knowledgeTitleIsNonUniqueButInOtherSoftware_shouldReturnFalse(){
        String knowledgeTitle = "Existing Knowledge";
        long softwareID = 1;
        long differentSoftwareID = 2;
        long knowledgeID = 2;

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(differentSoftwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeID, knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(Long knowledgeID, String knowledgeTitle, Long softwareID)
     * Description: Knowledge with the same title and software ID exists, but has a different knowledge ID.
     * Expected return: TRUE
     */
    @Test
    void findDuplicateWithKnowledgeId_knowledgeWithDifferentIdHasSameTitleAndSoftware_shouldReturnTrue() {
        String knowledgeTitle = "Existing Knowledge";
        long softwareID = 1;
        long knowledgeID = 2;

        Knowledge knowledge = new Knowledge();
        knowledge.setId(1L);
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(softwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertTrue(knowledgeBaseService.findDuplicate(knowledgeID, knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(Long knowledgeID, String knowledgeTitle, Long softwareID)
     * Description: Knowledge with the same title and software ID exists, but has the same knowledge ID.
     * Expected return: FALSE
     */
    @Test
    void findDuplicateWithKnowledgeId_knowledgeWithSameIdHasSameTitleAndSoftware_ShouldReturnFalse() {
        String knowledgeTitle = "Existing Knowledge";
        long softwareID = 1;
        long knowledgeID = 2;

        Knowledge knowledge = new Knowledge();
        knowledge.setId(knowledgeID);
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(softwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeID, knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(String knowledgeTitle, Long softwareID)
     * Description: Knowledge with the same title and software ID exists.
     * Expected return: TRUE
     */
    @Test
    void findDuplicate_knowledgeWithDifferentIdHasSameTitleAndSoftware_shouldReturnTrue() {
        String knowledgeTitle = "Existing Knowledge";
        long softwareID = 1;

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(softwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertTrue(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(String knowledgeTitle, Long softwareID)
     * Description: Knowledge has unique title. Test should return false.
     * Expected return: FALSE
     */
    @Test
    void findDuplicate_knowledgeTitleIsUnique_shouldReturnFalse(){
        String knowledgeTitle = "Non-existing Knowledge";
        long softwareID = 1;

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(null);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    /**
     * Method: boolean findDuplicate(String knowledgeTitle, Long softwareID)
     * Description: Knowledge with the same title exists, but has a different software ID.
     * Expected return: FALSE
     */
    @Test
    void findDuplicate_knowledgeTitleIsNonUniqueButInOtherSoftware_shouldReturnFalse(){
        String knowledgeTitle = "Existing Knowledge";
        long softwareID = 1;
        long differentSoftwareID = 2;

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(knowledgeTitle);
        knowledge.setSoftware(new Software());
        knowledge.getSoftware().setId(differentSoftwareID);

        when(knowledgeRepository.findByTitleIgnoreCase(knowledgeTitle)).thenReturn(knowledge);

        assertFalse(knowledgeBaseService.findDuplicate(knowledgeTitle, softwareID));
    }

    /**
     * Method: void update(UpdateKnowledgeRequest request)
     * Description: Tests the update method to ensure the knowledge entity is correctly updated based on the data from the UpdateKnowledgeRequest object.
     * Expected behavior:
     *  - The knowledge entity's title, content and associated software are updated.
     *  - The knowledge entity is saved in the repository.
     */
    @Test
    void update_ValidRequestIsProvided_ShouldUpdateKnowledgeSuccessfully() {
        long knowledgeID = 1;
        long softwareID = 2;

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
