package by.ilyatr.msaccountreservation.entity;

import by.ilyatr.msaccountreservation.entity.enums.ClientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private UUID id;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "citizenship", nullable = false)
  private String citizenship;

  @Column(name = "client_type", nullable = false)
  private String clientType;

  @Column(name = "document_number", nullable = false)
  private String documentNumber;

  @Column(name = "document_series", nullable = false)
  private String documentSeries;

  @Column(name = "document_type", nullable = false)
  private String documentType;

  @Column(name = "mdm_code", nullable = false)
  private Long mdmCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ClientStatus status;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
