# Contributing to Joot

Thank you for your interest in contributing to Joot! 🎉

## How to Contribute

### Reporting Issues

- Use [GitHub Issues](https://github.com/jtestkit/joot/issues)
- Provide a clear description and reproduction steps
- Include jOOQ version, database type, and Java version

### Suggesting Features

- Open a [GitHub Discussion](https://github.com/jtestkit/joot/discussions)
- Describe the use case and expected behavior
- Share example code if possible

### Submitting Pull Requests

1. **Fork** the repository
2. **Create a branch** for your feature/fix
3. **Write tests** for your changes
4. **Ensure all tests pass**: `./gradlew test`
5. **Submit a PR** with a clear description

### Code Style

- Follow existing code style
- Write clear, self-documenting code
- Add javadoc for public APIs
- Keep changes focused and atomic

### Testing

- All new features must have integration tests
- Tests should be isolated and repeatable
- Use meaningful test names: `shouldCreateBookWithAuthor()`

### Commit Messages

- Use clear, descriptive commit messages
- Start with a verb: "Add", "Fix", "Update", "Remove"
- Reference issues when applicable: "Fix #123"

## Development Setup

```bash
git clone https://github.com/jtestkit/joot.git
cd joot
./gradlew test
```

## Questions?

Feel free to ask in [GitHub Discussions](https://github.com/jtestkit/joot/discussions)!

---

**Thanks for contributing!** 🚀

