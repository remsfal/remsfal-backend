# CLAUDE.md

> Project context, architecture, commands, and conventions are maintained in [AGENT.md](./AGENT.md).
> Read AGENT.md before working in this codebase.

## Claude Code Specific Notes

- Use `@Authenticated` annotation on JAX-RS resources requiring login
- The auto-memory system at `.claude/` persists knowledge across sessions
