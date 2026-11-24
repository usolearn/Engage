# Azure DevOps setup and least privilege
Configuration proposal only; no Azure account, project, pipeline or user permission has been changed. Create a private ENGAGE project/repository and connect authorized access separately. Do not put secrets in the repository. Use an available free hosted-agent allocation if eligible; obtain owner approval before any paid capacity or services.

| Principal | Proposed scope |
|---|---|
| Owner | Project administrator, repository and pipeline administration |
| Maintainer | Contributor on project repo; branch changes via pull requests; no permission administration |
| Reviewer (after verified identity and invitation) | Read source; PR comments/review only as needed, no branch write or secrets access |
| Tester | Read approved pilot artifacts and file bugs only; no production signing credentials |
| Build identity | Project-scoped repo read, build artifacts write, no broad collection scope, no deployment permission |
| Public users | Approved published release only, not private project membership |

In Organization settings > Users, invite named users at the minimum access level supporting their role. Use Project settings > Permissions, Repositories > Security and Pipelines > Security for actual authorization; access levels and permissions are distinct. Protect main with review and successful build policies; withhold bypass permissions. Scope build job authorization to the current project, restrict pipeline access to explicitly authorized repositories, and limit secret/secure-file access to approved release pipelines. Verify effective permissions using test identities and negative tests before inviting reviewers. Do not grant access to external reviewers without verified account identity and owner approval of the actual invitation.
