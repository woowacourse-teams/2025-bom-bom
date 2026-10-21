"""Remove legacy DB connection values before packaging generated configuration."""
import sys
from pathlib import Path

import yaml

for name in sys.argv[1:]:
    path = Path(name)
    config = yaml.safe_load(path.read_text()) or {}
    spring = config.get("spring", {})
    datasource = spring.get("datasource", {})
    for key in ("url", "username", "password"):
        datasource.pop(key, None)
        config.pop(f"spring.datasource.{key}", None)
        spring.pop(f"datasource.{key}", None)
    path.write_text(yaml.safe_dump(config, allow_unicode=True, sort_keys=False))
