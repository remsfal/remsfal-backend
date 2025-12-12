# Authorization Concept

This document describes the role-based access control (RBAC) system used in the REMSFAL Backend.

## Table of Contents

- [Overview](#overview)
- [Role Definitions](#role-definitions)
- [Authorization Architecture](#authorization-architecture)

## Overview

REMSFAL implements a hierarchical, project-based authorization system with support for multiple contexts:

1. **Property Management Context**: Users have specific roles within projects (PROPRIETOR, MANAGER, etc.) who have full access to all information
2. **Tenant Context**: Users can be tenants with access to their rental information
3. **Contractor Context**: Users can be contractors that are commissioned by property managers and receive requests for quotations, submit offers and carry out orders

A user can have different roles in different projects and simultaneously be a tenant in one or more tenancies or be an employee in one or more contractor organizations.

## Role Definitions

### User Representation

A user represents exactly one natural person who has their own login via Google and can be identified by a unique email address.
This user may, in their capacity as a private individual, belong to an organization, own and/or manage a property, or be a tenant in a rental agreement (tenancy).
Each user is therefore stored as a unique entry in the users table.

```sql
SELECT * FROM users;
```

### Organization Representation

An organization represents a legal entity or a business that group users with specific roles.
Organizations can be assigned to projects, be contracting parties in a rental agreement, or work as contractors for a property management company, with their members automatically inheriting project/contractor-level permissions based on their organizational role.

The following five organizational types represent the most common use cases:

1. **Property Management Companies** (Hausverwaltungen)
2. **Owners' Associations** (Vermögensverwaltende GmbH)
3. **Housing Associations** (Wohnungsbaugesellschaft-Genossenschaft)
4. **Contractor Companies** (Handwerksbetriebe)
5. **Tenant Organizations** (Unternehmen als Mieter von Gewerbeflächen)

Each organization is stored as a unique entry in the organizations table.

```sql
SELECT * FROM organizations;
```

### Employee Roles

When an employee belongs to an organization, then one of the following three hierarchical roles must be assigned:

```java
public enum EmployeeRole {
    OWNER,      // Company owner/CEO
    MANAGER,    // Supervisor/Foreman
    STAFF;      // Worker/Employee
}
```

**Capabilities within Organization:**

| Role    | Create/Delete Organization | Manage Organization | Manage Employees | View Basic Information |
|---------|----------------------------|---------------------|------------------|------------------------|
| OWNER   | ✓ | ✓ | ✓ | ✓ |
| MANAGER | - | ✓ | ✓ | ✓ |
| STAFF   | - | - | - | ✓ |

Linking a user as an employee of an organization with the corresponding role is done via the organization_employees table.

```sql
SELECT * FROM organization_employees;
```

### Project Member Roles

Project member roles are hierarchical and assigned to users or organizations:

```java
public enum MemberRole {
    PROPRIETOR(10),    // Project owner - full control
    MANAGER(20),       // Project administrator - can manage project settings
    LESSOR(30),        // Lessor/Landlord - manages properties
    STAFF(40),         // Staff member - limited project access
    COLLABORATOR(50);  // External collaborator - read-only access

    private int leadership;
}
```

**Leadership Hierarchy:**
- Leadership levels determine privilege (lower number = higher privilege)
- **Privileged roles** (leadership ≤ 25): PROPRIETOR, MANAGER
  - Can modify project settings, add/remove members, manage properties
- **Standard roles** (leadership > 25): LESSOR, STAFF, COLLABORATOR
  - Read-only access to project data, can communicate with tenants and contractors

**Role Capabilities:**

| Role | Leadership | Create/Delete Project | Manage Members | Manage Properties | Manage Tenancies | Hire contractors | View Data |
|------|------------|-----------------------|----------------|-------------------|------------------|------------------|-----------|
| PROPRIETOR   | 10 | ✓ | ✓  | ✓ | ✓ | ✓ | ✓ |
| MANAGER      | 20 | - | ✓* | ✓ | ✓ | ✓ | ✓ |
| LESSOR       | 30 | - | -  | - | ✓ | ✓ | ✓ |
| STAFF        | 40 | - | -  | - | - | ✓ | ✓ |
| COLLABORATOR | 50 | - | -  | - | - | - | ✓ |

*MANAGER can add/change members but cannot remove PROPRIETOR or change/upgrade their own role

Linking a user as project member with the corresponding role is done via the project_memberships table.

```sql
SELECT * FROM project_memberships;
```


### Property Management Organization Roles

Property management organizations use the same role hierarchy as project memberships:

```java
public enum MemberRole {
    PROPRIETOR(10),    // Project owner - full control
    MANAGER(20),       // Project administrator - can manage project settings
    LESSOR(30),        // Lessor/Landlord - manages properties
    STAFF(40),         // Staff member - limited project access
    COLLABORATOR(50);  // External collaborator - read-only access

    private int leadership;
```

Linking an organization as project member with the corresponding role is done via the project_organizations table.

```sql
SELECT * FROM project_organizations;
```

Organizations can be assigned to projects, creating a relationship that grants their members access:

```
Organization → assigned to → Project
     ↓                          ↓
  Members                  Inherited Permissions
```

**Key principles:**
- Multiple organizations can work on the same project simultaneously
- One organization can be assigned to multiple projects
- Only PROPRIETOR and MANAGER can assign organizations
- Organizations cannot self-assign to projects

**Role Mapping (max(1):1)**

When an organization is assigned to a project, the roles of its members are directly assigned, while the organization's role in the project is resulting in the highest role of each project member:

```
Organization Role    →    Project Membership Role
─────────────────────────────────────────
PROPRIETOR          →    OWNER=PROPRIETOR, MANAGER=MANAGER, STAFF=STAFF
MANAGER             →    OWNER=MANAGER, MANAGER=MANAGER, STAFF=STAFF
LESSOR              →    OWNER=LESSOR, MANAGER=LESSOR, STAFF=STAFF
STAFF               →    OWNER=STAFF, MANAGER=STAFF, STAFF=STAFF
COLLABORATOR        →    OWNER=COLLABORATOR, MANAGER=COLLABORATOR, STAFF=COLLABORATOR
```

### Contractor Organization Roles

Contractors are always organizations with one or more employees that have the same three-tier role model as any other organization:

```java
public enum EmployeeRole {
    OWNER,      // Company owner/CEO
    MANAGER,    // Supervisor/Foreman
    STAFF;      // Worker/Employee
}
```

For an organization to become a contractor, this organization must either be added to the personal contractors list by a user or to the project-specific contractors list by a project member.

An organization is linked as a project-specific contractor via the project_contractors table.

```sql
SELECT * FROM project_contractors;
```

Or an organization is linked as a personal contractor via the user_contractors table.

```sql
SELECT * FROM user_contractors;
```

**Capabilities within Contractor Organization:**

| Role | Manage Company | Manage Quotation Requests | Create Offer | Accept Jobs | View Jobs Data | Communicate with Clients |
|------|----------------|---------------------------|--------------|-------------|----------------|--------------------------|
| OWNER   | ✓       | ✓ | ✓ | ✓ | ✓ | ✓ |
| MANAGER | Limited | ✓ | ✓ | ✓ | ✓ | ✓ |
| STAFF   | -       | - | - | - | ✓ | ✓ |


### Tenant Roles

Tenants, whether individuals or organizations, have no specific roles within a project or for collaboration. All employees of an organization are treated equally as tenants.
Tenants are linked to a corresponding rental unit via a rental agreement and thereby receive the right to open new issues or to communicate via these issues.


### User Roles

User roles represent functional categories in `UserJson.UserRole`:

```java
public enum UserRole {
    MANAGER,     // Property/project manager (project context)
    TENANT,      // Tenant/renter (tenancy context)
    CONTRACTOR   // Service provider/contractor
}
```

These roles are contextual:
- **MANAGER**: Derived from project membership with any MemberRole
- **TENANT**: Derived from tenancy membership
- **CONTRACTOR**: Derived from organizational employment

## Authorization Architecture

### JWT Token Structure

Access tokens contain authorization claims:

```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "name": "User Name",
  "active": true,
  "project_roles": {
    "project-uuid-1": "PROPRIETOR",
    "project-uuid-2": "COLLABORATOR"
  },
  "organization_roles": {
    "organization-uuid-1": "MANAGER",
    "organization-uuid-2": "STAFF"
  },
  "tenancy_projects": {
    "tenancy-uuid-1": "project-uuid-3"
  },
  "iss": "REMSFAL",
  "exp": 1234567890
}
```

**Claims:**
- `project_roles`: Map of all projects the user belongs to with their role
- `organization_roles`: Map of all organization the user belongs to with their role
- `tenancy_projects`: Map of tenancies to projects where user or its organization is a tenant

### Principal Injection

The `RemsfalPrincipal` provides access to the authenticated user and their authorization context:

```java
@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    public UUID getId();                                 // User ID
    public String getEmail();                            // User email
    public Map<UUID, MemberRole> getProjectRoles();      // Project memberships
    public Map<UUID, MemberRole> getOrganizationRoles(); // Organization employment
    public Map<UUID, UUID> getTenancyProjects();         // Tenant assignments
}
```

All authenticated resources inject this principal:

```java
@Authenticated
public class SomeResource {
    @Inject
    protected RemsfalPrincipal principal;
}
```

### Authentication Flow

```
User → Google OAuth → Platform Service
                          ↓
                  Generate JWT with roles
                          ↓
            Store in secure HTTP-only cookie
                          ↓
          Subsequent requests auto-validated
```

### Token Security

- **Algorithm:** RSA256 (asymmetric signing)
- **Transport:** Secure, HTTP-only cookies with SameSite=STRICT
- **Access Token TTL:** 25 minutes
- **Refresh Token TTL:** 7 days
- **Auto-refresh:** 5 minutes before expiry

### Multi-Service Validation

Services validate tokens using the platform's JWKS endpoint:

```
Ticketing/Notification Service → GET /api/v1/authentication/jwks
                                      ↓
                              Platform returns public key
                                      ↓
                              Service validates JWT locally
```

### Role Hierarchy Protection

The leadership level system prevents privilege escalation:

```java
// A MANAGER (leadership=20) cannot:
// - Promote themselves to PROPRIETOR
// - Remove/demote the PROPRIETOR
// - Create a new PROPRIETOR

// Only the existing PROPRIETOR can transfer ownership
```
