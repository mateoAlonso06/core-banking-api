## Currency Invariants

- An Account is associated with exactly one currency
- All transactions for an account must use the same currency
- Transfers are only allowed between accounts with the same currency
- Currency exchange is modeled as a separate use case

## Account Invariants

- A customer may have multiple ARS accounts.
- A customer may have at most one USD account.
- Attempting to create a second USD account for the same customer
- must fail the domain boundary (application service) with a bussiness error.