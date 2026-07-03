package com.example.shopwiseapi.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loyalty_accounts")
@EntityListeners(AuditingEntityListener.class)
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Builder.Default
    @Column(nullable = false)
    private Integer pointsBalance = 0;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void creditPoints(Integer points) {
        pointsBalance += points;
    }

    public void debitPoints(Integer points) {
        pointsBalance -= points;
    }
}
