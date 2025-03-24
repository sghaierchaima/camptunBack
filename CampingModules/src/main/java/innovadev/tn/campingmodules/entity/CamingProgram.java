package innovadev.tn.campingmodules.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import innovadev.tn.campingmodules.enums.Category;
import innovadev.tn.campingmodules.enums.ProgramStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "camping_programs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CamingProgram implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long organizerId;  // ID de l'organisateur

    private String title;
    private String description;
    private String location;
    @Column(nullable = true)
    private String imageUrl;


    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private ProgramStatus status;

    private Date startDate;
    private Date endDate;
    private float price;
    private int maxParticipants;
    private int nParticipants;
    private String contactEmail;
    private String contactPhone;
    private boolean providesTents;
    private boolean providesSleepingBags;
    private boolean providesEquipment;
    private boolean providesTransport;
    private boolean providesFood;
    private String agenda;

    public CamingProgram(String title, String description, String location, String startDate, String endDate, String category, String price, String maxParticipants, String contactEmail, String contactPhone, boolean providesTents, boolean providesSleepingBags, boolean providesEquipment, boolean providesTransport, boolean providesFood, String status, String agenda) {
    }

}
