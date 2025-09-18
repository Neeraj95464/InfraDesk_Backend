//package com.InfraDesk.entity;
//
//import com.InfraDesk.enums.SubscriptionPlan;
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedBy;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "subscriptions")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Subscription {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id", nullable = false)
//    private Company company;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private SubscriptionPlan plan;
//
//    @Column(nullable = false)
//    private LocalDateTime startDate;
//
//    @Column(nullable = false)
//    private LocalDateTime endDate;
//
//    @Column(nullable = false)
//    private Double amountPaid;
//
//    private String paymentMode; // e.g., Razorpay, Stripe, UPI
//
//    private String paymentReferenceId;
//
//    private Boolean isTrial = false;
//
//    @Column(nullable = false)
//    private Boolean isActive = true;
//
//    private Integer maxUsersAllowed;
//
//    private Integer maxAssetsAllowed;
//
//    @CreatedBy
//    private String createdBy;
//
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @Column(nullable = false)
//    private Boolean isDeleted = false;
//}
//


package com.InfraDesk.entity;

import com.InfraDesk.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A subscription always belongs to one company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Double amountPaid;

    private String paymentMode; // Razorpay, Stripe, UPI

    private String paymentReferenceId;

    private Boolean isTrial = false;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer maxUsersAllowed;

    private Integer maxAssetsAllowed;

    @CreatedBy
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isDeleted = false;
}
