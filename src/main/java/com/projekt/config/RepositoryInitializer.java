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
    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;

    public RepositoryInitializer(UserRepository userRepository, RoleRepository roleRepository,
                                 SoftwareRepository softwareRepository, KnowledgeRepository knowledgeRepository,
                                 CategoryRepository categoryRepository, PriorityRepository priorityRepository,
                                 StatusRepository statusRepository, ImageRepository imageRepository,
                                 TicketRepository ticketRepository, TicketReplyRepository ticketReplyRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.softwareRepository = softwareRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.categoryRepository = categoryRepository;
        this.priorityRepository = priorityRepository;
        this.statusRepository = statusRepository;
        this.imageRepository = imageRepository;
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
                software.setName("Steam");
                software.setDescription("Digital distribution and rights management platform, multiplayer game system and social network created by Valve Corporation");
                softwareRepository.save(software);

                Software software1 = new Software();
                software1.setId(2L);
                software1.setName("Origin");
                software1.setDescription("A platform from Electronic Arts that acts as a digital game shop and digital access control system.");
                softwareRepository.save(software1);

                Software software2 = new Software();
                software2.setId(3L);
                software2.setName("GOG");
                software2.setDescription("Digital distribution service for computer games, created in 2008 by the Polish company CDP Investment sp. z o.o.");
                softwareRepository.save(software2);

                Knowledge knowledge = new Knowledge();
                knowledge.setId(1L);
                knowledge.setTitle("What is Steam?");
                knowledge.setContent(software.getDescription());
                knowledge.setSoftware(softwareRepository.getReferenceById(1L));
                knowledgeRepository.save(knowledge);

                Knowledge knowledge1 = new Knowledge();
                knowledge1.setId(2L);
                knowledge1.setTitle("What is Origin?");
                knowledge1.setContent(software2.getDescription());
                knowledge1.setSoftware(softwareRepository.getReferenceById(2L));
                knowledgeRepository.save(knowledge1);

                Knowledge knowledge2 = new Knowledge();
                knowledge2.setId(3L);
                knowledge2.setTitle("When was Steam created?");
                knowledge2.setContent("The platform began development in 2002. The final version of the Steam client was released on 12 September 2003.");
                knowledge2.setSoftware(softwareRepository.getReferenceById(1L));
                knowledgeRepository.save(knowledge2);
            }

            if(categoryRepository.findAll().isEmpty()){
                categoryRepository.save(new Category(1L, "General"));
                categoryRepository.save(new Category(2L, "Question"));
                categoryRepository.save(new Category(3L, "Bug"));
                categoryRepository.save(new Category(4L, "Suggestion"));
            }

            if(priorityRepository.findAll().isEmpty()){
                priorityRepository.save(new Priority(1L,"High",1));
                priorityRepository.save(new Priority(2L, "Normal",2));
                priorityRepository.save(new Priority(3L,"Low", 5));
            }

            if(statusRepository.findAll().isEmpty()){
                statusRepository.save(new Status(1L,"New",false));
                statusRepository.save(new Status(2L, "In progress", false));
                statusRepository.save(new Status(3L,"Closed", true));
            }

            if(ticketReplyRepository.findAll().isEmpty() && ticketRepository.findAll().isEmpty() && imageRepository.findAll().isEmpty()){
                Ticket ticket = new Ticket();
                ticket.setId(1L);
                ticket.setTitle("The website is unreachable");
                ticket.setStatus(statusRepository.getReferenceById(1L));
                ticket.setPriority(priorityRepository.getReferenceById(2L));
                ticket.setSoftware(softwareRepository.getReferenceById(3L));
                ticket.setUser(userRepository.getReferenceById(1L));
                ticket.setDescription("When trying to enter the site, I get an error - This site is unreachable :(");
                ticket.setVersion("1.0");

                ticket.setCategory(categoryRepository.getReferenceById(1L));

                Image image = new Image(1L,"1.png",null);
                try {
                    BufferedImage bi = ImageIO.read(getClass().getResourceAsStream("/images/1.png"));
                    image.setContent(toByteArray(bi));
                } catch (IOException e) {
                    image.setContent(createEmptyImage());
                }

                imageRepository.save(image);

                List<Image> imageList = new ArrayList<>();
                imageList.add(image);
                ticket.setImages(imageList);

                TicketReply ticketReply = new TicketReply(1L,userRepository.getReferenceById(2L),"Have you checked that you have entered the correct address?", LocalDate.of(2021,12,11));
                TicketReply ticketReply1 = new TicketReply(2L, userRepository.getReferenceById(1L), "Yes, it is correct", LocalDate.of(2021,12,12));
                ticketReplyRepository.save(ticketReply);
                ticketReplyRepository.save(ticketReply1);

                List<TicketReply> ticketReplyList = new ArrayList<>();
                ticketReplyList.add(ticketReply);
                ticketReplyList.add(ticketReply1);
                ticket.setReplies(ticketReplyList);

                ticketRepository.save(ticket);
            }
        };
    }

    private byte[] toByteArray(BufferedImage bi) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", stream);

        return stream.toByteArray();
    }

    private byte[] createEmptyImage() throws IOException {
        BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return toByteArray(emptyImage);
    }
}
