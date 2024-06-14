package com.projekt.config;

import com.projekt.models.*;
import com.projekt.repositories.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Configuration
public class RepositoryInitializer {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SoftwareRepository softwareRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final CategoryRepository categoryRepository;
    private final PriorityRepository priorityRepository;
    private final StatusRepository statusRepository;
    private final ImageRepository imageRepository;
    private final VersionRepository versionRepository;
    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;

    public RepositoryInitializer(UserRepository userRepository, RoleRepository roleRepository, SoftwareRepository softwareRepository, KnowledgeRepository knowledgeRepository, CategoryRepository categoryRepository, PriorityRepository priorityRepository, StatusRepository statusRepository, ImageRepository imageRepository, VersionRepository versionRepository, TicketRepository ticketRepository, TicketReplyRepository ticketReplyRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.softwareRepository = softwareRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.categoryRepository = categoryRepository;
        this.priorityRepository = priorityRepository;
        this.statusRepository = statusRepository;
        this.imageRepository = imageRepository;
        this.versionRepository = versionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketReplyRepository = ticketReplyRepository;
    }

    @Bean
    InitializingBean init() {
        return () -> {
            if(userRepository.findAll().isEmpty() && roleRepository.findAll().isEmpty()){
                Role roleUser = roleRepository.save(new Role(Role.Types.ROLE_USER));
                Role roleOperator = roleRepository.save(new Role(Role.Types.ROLE_OPERATOR));
                Role roleAdmin = roleRepository.save(new Role(Role.Types.ROLE_ADMIN));

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

                User user = new User("user", true);
                user.setRoles(new HashSet<>(List.of(roleUser)));
                user.setEmail("example1@gmail.com");
                user.setName("Jan");
                user.setSurname("Kowalski");
                user.setPassword(passwordEncoder.encode("user"));

                User operator = new User("operator", true);
                operator.setRoles(new HashSet<>(Arrays.asList(roleOperator,roleUser)));
                operator.setEmail("example2@gmail.com");
                operator.setName("Adam");
                operator.setSurname("Nowak");
                operator.setPassword(passwordEncoder.encode("operator"));

                User admin = new User("admin", true);
                admin.setRoles(new HashSet<>(Arrays.asList(roleAdmin, roleOperator,roleUser)));
                admin.setEmail("example3@gmail.com");
                admin.setName("Piotr");
                admin.setSurname("Kowalski");
                admin.setPassword(passwordEncoder.encode("admin"));

                userRepository.save(user);
                userRepository.save(operator);
                userRepository.save(admin);
            }

            if(softwareRepository.findAll().isEmpty() && knowledgeRepository.findAll().isEmpty()){
                Software software = new Software();
                software.setId(1L);
                software.setSoftwareName("Steam");
                software.setSoftwareDescription("Platforma dystrybucji cyfrowej i zarządzania prawami cyfrowymi, system gry wieloosobowej oraz serwis społecznościowy stworzony przez Valve Corporation");
                softwareRepository.save(software);

                Software software1 = new Software();
                software1.setId(2L);
                software1.setSoftwareName("Origin");
                software1.setSoftwareDescription("Platforma firmy Electronic Arts pełniąca funkcję sklepu z grami w wersji cyfrowej oraz systemu kontroli dostępu do danych w formie cyfrowej.");
                softwareRepository.save(software1);

                Software software2 = new Software();
                software2.setId(3L);
                software2.setSoftwareName("GOG");
                software2.setSoftwareDescription("Serwis cyfrowej dystrybucji gier komputerowych, stworzony w 2008 roku przez polską firmę CDP Investment sp. z o.o.");
                softwareRepository.save(software2);

                Knowledge knowledge = new Knowledge();
                knowledge.setKnowledgeID(1);
                knowledge.setKnowledgeTitle("Co to jest Steam?");
                knowledge.setKnowledgeContent(software.getSoftwareDescription());
                knowledge.setKnowledgeDate(LocalDate.now());
                knowledge.setSoftware(softwareRepository.getReferenceById(1L));
                knowledgeRepository.save(knowledge);

                Knowledge knowledge1 = new Knowledge();
                knowledge1.setKnowledgeID(2);
                knowledge1.setKnowledgeTitle("Co to jest Origin?");
                knowledge1.setKnowledgeContent(software2.getSoftwareDescription());
                knowledge1.setKnowledgeDate(LocalDate.of(2022,1,15));
                knowledge1.setSoftware(softwareRepository.getReferenceById(2L));
                knowledgeRepository.save(knowledge1);

                Knowledge knowledge2 = new Knowledge();
                knowledge2.setKnowledgeID(3);
                knowledge2.setKnowledgeTitle("Kiedy powstał Steam?");
                knowledge2.setKnowledgeContent("Platformę zaczęto rozwijać w 2002 roku. Finalną wersję klienta Steam wydano 12 września 2003.");
                knowledge2.setKnowledgeDate(LocalDate.of(2022,1,10));
                knowledge2.setSoftware(softwareRepository.getReferenceById(1L));
                knowledgeRepository.save(knowledge2);
            }

            if(categoryRepository.findAll().isEmpty()){
                categoryRepository.save(new Category(1, "Ogólne"));
                categoryRepository.save(new Category(2, "Pytanie"));
                categoryRepository.save(new Category(3, "Błąd"));
                categoryRepository.save(new Category(4, "Sugestia"));
            }

            if(priorityRepository.findAll().isEmpty()){
                priorityRepository.save(new Priority(1,"Wysoki",1));
                priorityRepository.save(new Priority(2, "Normalny",2));
                priorityRepository.save(new Priority(3,"Niski", 5));
            }

            if(statusRepository.findAll().isEmpty()){
                statusRepository.save(new Status(1,"Nowe",false));
                statusRepository.save(new Status(2, "W realizacji", false));
                statusRepository.save(new Status(3,"Zamknięte", true));
            }

            if(versionRepository.findAll().isEmpty()){
                Version version = new Version(1,2020,10,18,softwareRepository.getReferenceById(1L));
                versionRepository.save(version);
            }

            if(ticketReplyRepository.findAll().isEmpty() && ticketRepository.findAll().isEmpty() && imageRepository.findAll().isEmpty()){
                Ticket ticket = new Ticket();
                ticket.setTicketID(1);
                ticket.setTicketDate(LocalDate.of(2021,12,10));
                ticket.setUser(userRepository.getReferenceById(1));
                ticket.setTicketTitle("Witryna jest nieosiągalna");
                ticket.setStatus(statusRepository.getReferenceById(1));
                ticket.setPriority(priorityRepository.getReferenceById(2));
                ticket.setTicketDescription("Podczas próby wejścia na wskazaną na zrzucie ekranu podstronę, otrzymuję błąd - Ta witryna jest nieosiągalna :(");

                Set<Category> categorySet = new HashSet<>();
                categorySet.add(categoryRepository.getReferenceById(1));
                ticket.setCategories(categorySet);

                BufferedImage bi = ImageIO.read(new File("src/main/resources/images/1.png"));
                Image image = new Image(1,"1.png",toByteArray(bi, "png"));
                imageRepository.save(image);

                List<Image> imageList = new ArrayList<>();
                imageList.add(image);
                ticket.setImages(imageList);

                TicketReply ticketReply = new TicketReply(1,userRepository.getReferenceById(2),"Sprawdzałeś, czy dobrze wpisujesz adres?", LocalDate.of(2021,12,11));
                TicketReply ticketReply1 = new TicketReply(2, userRepository.getReferenceById(1), "Tak, jest poprawny", LocalDate.of(2021,12,12));
                ticketReplyRepository.save(ticketReply);
                ticketReplyRepository.save(ticketReply1);

                List<TicketReply> ticketReplyList = new ArrayList<>();
                ticketReplyList.add(ticketReply);
                ticketReplyList.add(ticketReply1);
                ticket.setTicketReplies(ticketReplyList);

                ticket.setVersion(versionRepository.getReferenceById(1));

                ticketRepository.save(ticket);
            }
        };
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);

        return baos.toByteArray();
    }
}
