package by.ilyatr.msaccountreservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "full_name")
  private String fullName;

  private String citizenship;

  @Column(name = "client_type")
  private String clientType;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "document_series")
  private String documentSeries;

  @Column(name = "document_type")
  private String documentType;

  @Column(name = "mdm_code")
  private Long mdmCode;
}