# Modele de domaine ShopWise

Ce diagramme represente les entites JPA actuellement implementees. Les donnees
metier sont isolees par `Business`. Un `MerchantAccount` accede a un ou plusieurs
commerces par l'intermediaire de `BusinessMembership`.

```mermaid
classDiagram
direction LR

class Business {
  +Long id
  +String name
  +String email UK
  +String phone
  +Boolean active
}

class MerchantAccount {
  +Long id
  +String firstName
  +String lastName
  +String email UK
  +String passwordHash
  +Boolean active
  +LocalDateTime lastLogin
  +LocalDateTime createdAt
}

class BusinessMembership {
  +Long id
  +MembershipRole role
  +Boolean active
}

class MerchantInvitation {
  +Long id
  +String email
  +MembershipRole role
  +String token UK
  +LocalDateTime expiresAt
  +InvitationStatus status
  +LocalDateTime createdAt
  +LocalDateTime acceptedAt
}

class Client {
  +Long id
  +String firstName
  +String lastName
  +String email UK
  +String phone
  +Boolean active
  +LocalDateTime createdAt
  +LocalDateTime updatedAt
}

class CustomerAccount {
  +Long id
  +String passwordHash
  +Boolean active
  +LocalDateTime lastLogin
}

class Service {
  +Long id
  +String name
  +String description
  +Integer durationMinutes
  +Integer loyaltyPoints
  +Boolean active
}

class Appointment {
  +Long id
  +LocalDateTime startsAt
  +LocalDateTime endsAt
  +AppointmentStatus status
  +String comment
  +LocalDateTime createdAt
  +LocalDateTime updatedAt
  +markAsHonored()
  +cancel()
}

class LoyaltyAccount {
  +Long id
  +Integer pointsBalance
  +LocalDateTime updatedAt
  +creditPoints()
  +debitPoints()
}

class LoyaltyTransaction {
  +Long id
  +TransactionType type
  +Integer pointsDelta
  +String reason
  +LocalDateTime transactionDate
}

class MembershipRole {
  <<enumeration>>
  OWNER
  MANAGER
  STAFF
}

class InvitationStatus {
  <<enumeration>>
  PENDING
  ACCEPTED
  REVOKED
  EXPIRED
}

class AppointmentStatus {
  <<enumeration>>
  SCHEDULED
  HONORED
  CANCELED
}

class TransactionType {
  <<enumeration>>
  EARNED
  REDEEMED
  ADJUSTMENT
}

Business "1" --> "0..*" BusinessMembership : memberships
MerchantAccount "1" --> "0..*" BusinessMembership : accesses
BusinessMembership --> MembershipRole : role

Business "1" --> "0..*" MerchantInvitation : invitations
MerchantAccount "1" --> "0..*" MerchantInvitation : creates
MerchantInvitation --> MembershipRole : requested role
MerchantInvitation --> InvitationStatus : status

Business "1" --> "0..*" Client : clients
Business "1" --> "0..*" Service : services
Business "1" --> "0..*" Appointment : appointments

Client "1" --> "0..1" CustomerAccount : customer login
Client "1" --> "0..1" LoyaltyAccount : loyalty
Client "1" --> "0..*" Appointment : books

Service "1" --> "0..*" Appointment : concerns
MerchantAccount "0..1" --> "0..*" Appointment : created by
Appointment --> AppointmentStatus : status

LoyaltyAccount "1" --> "0..*" LoyaltyTransaction : transactions
Appointment "0..1" --> "0..1" LoyaltyTransaction : generates
LoyaltyTransaction --> TransactionType : type
```

## Contraintes metier representees

- Un rattachement `(merchantAccount, business)` est unique.
- Les roles d'un rattachement sont `OWNER`, `MANAGER` ou `STAFF`.
- Une entreprise doit conserver au moins un `OWNER` actif.
- Une invitation possede un jeton unique, une expiration et un statut de cycle de vie.
- Un client, une prestation, un rendez-vous et le compte de fidelite associe appartiennent a un seul commerce.
- Le createur d'un rendez-vous est le `MerchantAccount` de la session, pas l'ancienne entite `Merchant`.
- Un client peut avoir au maximum un `CustomerAccount` et un `LoyaltyAccount`.
- Un rendez-vous peut generer au maximum une `LoyaltyTransaction`.
- Chaque nouveau commerce recoit les trois prestations par defaut configurees par le backend.
