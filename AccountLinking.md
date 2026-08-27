# Account Linking & Data Protection

This document describes how a REMSFAL user account (`UserEntity`) can become linked to tenants, project
memberships, and organizations, how that linking is established and undone, and the data protection
considerations that follow from it. For the role-based access control system built on top of these links, see
[Authorization.md](Authorization.md).

## Table of Contents

- [Overview](#overview)
- [Active vs. Placeholder Accounts](#active-vs-placeholder-accounts)
- [Linkage Types](#linkage-types)
- [Email as the Linking Key](#email-as-the-linking-key)
- [Data Protection Considerations](#data-protection-considerations)

## Overview

A `UserEntity` represents exactly one natural person, identified by a unique `email` address. Depending on
context, that person can additionally be:

- a **tenant** of one or more rental units (`TenantEntity`),
- a **member** of one or more projects (`ProjectMembershipEntity`),
- an **employee** of one or more organizations (`OrganizationEmployeeEntity`), including organizations that act
  as contractors.

Structurally, two different linking patterns are used:

1. **Mandatory join-table links** — `project_memberships` and `organization_employees`. The row's primary key
   *is* the `(resource_id, user_id)` pair, so the link cannot exist without a concrete `UserEntity`. Deleting the
   user cascades and removes the link.
2. **Optional, nullable FK link** — `tenants.user_id` and, identically, `contractors.organization_id`. A tenant
   (respectively contractor) is an independent record that may or may not be linked to a user account
   (respectively organization). The link is established and re-validated by matching email addresses, and
   deleting the linked user/organization only detaches the link (`SET NULL`); the tenant/contractor record
   itself survives.

Contractors are never linked to a user directly — a contractor is linked to an *organization*
(`contractors.organization_id`, optional, matched by email exactly like Tenant ↔ User below), and that
organization's employees inherit the `CONTRACTOR` context through their `organization_employees` membership.
There is no separate contractor-to-user join table.

## Active vs. Placeholder Accounts

`UserEntity.isActive()` returns `true` if and only if `tokenId` (the Google account identifier) is set. This
distinguishes two kinds of account:

- **Active account** — created by `UserController.createUser(googleId, email)` on first successful Google login
  (`AuthorizationController.authenticateUser`). `tokenId`, `locale`, and `authenticatedAt` are set.
- **Placeholder account** — created by `UserController.findOrCreateUser(UserModel)` when someone invites a person
  by email who has no account yet (`ProjectController.addProjectMember`, `OrganizationController.addEmployee`,
  `OrganizationController.createOrganization`). Only `id` and `email` are stored; `tokenId` stays `null`. Nothing
  else about that person (name, address, phone, locale) is populated at this point.

### Reclaim on first login

Because a placeholder's `id` may already be referenced by `project_memberships`, `organization_employees`, or
`tenants.user_id`, the first real login by that person must reuse the *same* row rather than create a new one —
otherwise the insert would violate the unique constraint on `users.email` and every previously established link
would be orphaned.

`UserController.createUser` therefore looks up an existing row by (normalized) email before creating anything:

- if a row exists and is **already active**, the login attempt is rejected with `AlreadyExistsException` (a
  genuine email conflict between two different Google accounts);
- if a row exists and is **inactive** (a placeholder), it is *reclaimed*: `tokenId`, `locale`
  (derived from the request's `Accept-Language` header, see below), and `authenticatedAt` are set on the
  existing entity, which is then merged — the `id`, and therefore every link established while the account was a
  placeholder, is preserved;
- otherwise a brand-new active account is created.

In both the "brand-new" and the "reclaimed" case, `NotificationController.informUserAboutRegistration(...)` is
sent, in the locale that was just derived for that account.

## Linkage Types

| Link | Table | Cardinality | Match key | On user deletion |
|------|-------|-------------|-----------|-------------------|
| Tenant ↔ User | `tenants.user_id` | Optional (`@OneToOne`, nullable FK) | `tenants.email` (own column, no fallback to the linked user's email) | `SET NULL` — tenant record survives |
| Project Membership ↔ User | `project_memberships` | Mandatory (composite PK `project_id, user_id`) | User `id` (or email via `findOrCreateUser` when inviting) | `CASCADE` — membership row is deleted |
| Organization Employee ↔ User | `organization_employees` | Mandatory (composite PK `organization_id, employee_id`) | User `id` (or email via `findOrCreateUser` when inviting) | `CASCADE` — membership row is deleted |
| Contractor ↔ Organization | `contractors.organization_id` | Optional (`@ManyToOne`, nullable FK — the same organization may be a contractor on many projects, at most once per project) | `contractors.email` (own column, no fallback to the linked organization's email); re-evaluated by `ContractorOrganizationLinkController.relinkByEmail` on contractor creation and whenever a contractor's email changes. `organizationId` is server-derived only — a client cannot set it directly (unlike `userId` on tenants, which also isn't client-settable) | `SET NULL` — contractor record survives |
| Contractor ↔ User | *(none — derived)* | via Organization Employee | `ContractorRepository.existsByOrganizationEmployeeUserId`: true if the user is an employee of an organization that is `organizationId` on at least one contractor | n/a |

`tenants` additionally carries two partial unique indexes, `uq_tenant_project_user` and `uq_tenant_project_email`
(both `WHERE ... IS NOT NULL`), so a user can be linked to at most one tenant per project, and an email can be
used by at most one tenant per project. `organizations` carries a plain unique index, `uq_organization_email`
(`WHERE email IS NOT NULL`), so an email can be used by at most one organization system-wide — this is what
makes it a reliable matching key for contractor linking. `contractors` mirrors the tenant pattern with
`uq_contractor_project_organization` and `uq_contractor_project_email` (both `WHERE ... IS NOT NULL`), so an
organization can be linked to at most one contractor per project, and an email can be used by at most one
contractor per project.

### Re-validation on change

Whenever a tenant's email is changed (creation or PATCH), `TenantUserLinkController.relinkByEmail` re-evaluates
the user link: it drops a stale link if the new email no longer matches the linked user, then attempts to link
the tenant to whichever user (if any) now owns that email — but only if that user is not already linked to a
different tenant in the same project, which would otherwise violate `uq_tenant_project_user`. A conflict here
surfaces as `409 AlreadyExistsException` rather than a raw database error.

Contractor ↔ Organization linking works identically, via `ContractorOrganizationLinkController.relinkByEmail`,
called from `ContractorController` on contractor creation and whenever a contractor's email changes on PATCH: it
drops a stale organization link if the email no longer matches, then links to whichever organization (if any)
now owns that email — unless that organization is already linked to a different contractor in the same project
(`uq_contractor_project_organization`), which likewise surfaces as `409 AlreadyExistsException`.

### Contractor-relevant organization updates → self-service issue

Symmetric to the tenant flow described below, when an organization's own profile fields change
(`OrganizationController.updateOrganization`, not counting a no-op update where every field was omitted),
`OrganizationEventProducer` publishes an `ORGANIZATION_UPDATED` event on the `organization-events` topic,
carrying the updated `OrganizationJson` and one `AffectedContractorJson` (contractor id + project id) per
contractor record currently linked to that organization. `remsfal-ticketing`'s `OrganizationEventConsumer`
creates one `SELF_SERVICE` issue per affected contractor, storing a `ContractorJson` snapshot derived from the
organization's new data in `issues.contractor_update` (mirroring `issues.tenant_update` below) for a project
manager to review.

## Email as the Linking Key

Email addresses are the primary matching key across every linkage type above, so their integrity directly
determines whether the correct real-world person ends up linked to the correct records:

- All entity setters (`UserEntity`, `TenantEntity`, `ContractorEntity`, `OrganizationEntity`,
  `AdditionalEmailEntity`) normalize email to lowercase (`trim().toLowerCase()`) on write.
- The corresponding lookup methods (`UserRepository.findByEmail`, `TenantRepository.findByEmailAndProjectId`,
  `AdditionalEmailRepository.existsByEmail`) normalize the same way on read, so a lookup can never be defeated by
  case alone.
- `users.email` carries a database-level unique constraint; `tenants` carries the two partial unique indexes
  described above.

Without this normalization, `Jane@Example.org` and `jane@example.org` could have silently created two distinct
identities for the same person — see [Data Protection Considerations](#data-protection-considerations).

## Data Protection Considerations

Because account linking works by matching email addresses supplied by *someone other than the data subject*
(an inviter, a property manager entering tenant data), several points are relevant beyond the technical
mechanics:

1. **Personal data is processed before the data subject acts.** Inviting a project member or organization
   employee, or entering a tenant's contact details, stores that person's email (and, for tenants, potentially
   name, date of birth, address, and phone numbers) *before* they have ever interacted with REMSFAL. This
   processing rests on a different legal basis than the inviter's own account (typically the underlying
   contractual/rental relationship, not the invitee's consent) and should be reflected in the privacy notice
   shown to inviters and, where practical, communicated to the invitee.
2. **Placeholder accounts are data-minimal by construction.** `findOrCreateUser` stores only `id` and `email` —
   no profile data is collected on the invited person's behalf. Profile data is only added once that person logs
   in themselves.
3. **Reclaiming an account does not collect additional data.** The first-login reclaim path
   (`UserController.createUser`) sets only `tokenId`, `locale`, and `authenticatedAt`; it does not read the
   Google profile's name or picture. Name/address/phone remain fields the user fills in themselves via
   `updateUser`.
4. **Deletion behaviour is intentionally inconsistent across link types, which matters for erasure requests
   (GDPR Art. 17).** `UserController.deleteUser` hard-deletes the `users` row and emits a `USER_DELETED` event.
   - Cascade-linked data — `project_memberships`, `organization_employees`, `user_authentications`,
     `user_additional_email` — is removed automatically with the user.
   - `tenants` and `contractors` are **not** deleted; their FK to the user is set to `NULL` and the record's own
     data (name, DOB, address, phone for tenants; company details for contractors) persists. This is deliberate:
     rental and contractor records typically have an independent retention basis (accounting/legal retention
     obligations), but it means "delete my account" does not remove data the person may reasonably expect to be
     gone, and should be communicated as such.
   - The ticketing service reacts to `USER_DELETED` by clearing issue assignments
     (`IssueRepository.clearAssigneeAndResetStatus`) rather than deleting the issues — ticket content referencing
     the deleted user is retained but no longer attributed to an active account.
5. **Only additional emails are cryptographically verified.** An additional/alternate email
   (`AdditionalEmailEntity`) only becomes trusted after a 24‑hour, single-use verification token is confirmed
   (`UserController.verifyAdditionalEmail`); `isVerifiedEmailForUser` gates uses like assigning an organization's
   display email. The *primary* linking email used for tenant/project/organization invites has no equivalent
   verification step — an inviter's claim that "this email belongs to this person" is trusted outright, and the
   actual owner only implicitly confirms it by choosing to log in with the matching Google account. Until that
   happens, the data exists under an unconfirmed placeholder identity.
6. **Normalization and uniqueness are data-integrity controls, not just a lookup convenience.** Without
   case-insensitive matching and the unique constraints described above, the same natural person could end up
   represented by two different `UserEntity`/`TenantEntity` rows purely due to inconsistent capitalization —
   which is both a data-minimization problem (duplicate storage of one person) and a correctness risk for
   support, export, or erasure requests that only look up one of the duplicates.

### Open items worth revisiting

- Whether invited-but-never-confirmed placeholder accounts should be purged after a retention period if the
  invitee never logs in.
- Whether the consent/privacy flow shown to an inviter should explicitly disclose that entering a third party's
  email address causes that party's personal data to be processed.
