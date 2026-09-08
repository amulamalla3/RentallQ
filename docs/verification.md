# Verification notes

Checks completed in the generation environment:

- Python syntax check passed.
- ML service tests passed: 3/3.
- `pom.xml` parsed as valid XML.
- `package.json` and `tsconfig.json` parsed as valid JSON.
- `docker-compose.yml` and GitHub Actions workflow parsed as valid YAML.

The generation environment could not reach public Maven/npm/PyPI registries, so a complete dependency download and Docker build could not be executed here. On a normal development machine with internet access, run:

```bash
docker compose up --build
```

Then verify:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health
curl http://localhost:8080/api/properties
curl -X POST http://localhost:8080/api/properties/1/analyze
```

Open `http://localhost:3000` for the UI.
