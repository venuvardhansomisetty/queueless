package com.queueless.model;

import jakarta.persistence.*;

// One service people can queue for (example: "Fee Counter", "Library Card").
// Named ServiceItem (not Service) so it is not confused with Spring's @Service.
@Entity
@Table(name = "services")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Average minutes needed to serve one person. Used for the estimated waiting time.
    @Column(name = "avg_service_minutes", nullable = false)
    private int avgServiceMinutes = 5;

    // The staff member who manages this service (set by the admin). Can be empty at first.
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAvgServiceMinutes() { return avgServiceMinutes; }
    public void setAvgServiceMinutes(int avgServiceMinutes) { this.avgServiceMinutes = avgServiceMinutes; }
    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }
}
