#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────
#  LocalStack POC — Setup Check
#  Verifies that all required tools are installed before
#  running docker-compose up --build
# ──────────────────────────────────────────────────────────

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

PASS="${GREEN}PASS${NC}"
FAIL="${RED}FAIL${NC}"
WARN="${YELLOW}WARN${NC}"

required_ok=true

header() {
  echo ""
  echo -e "${CYAN}${BOLD}═══════════════════════════════════════════${NC}"
  echo -e "${CYAN}${BOLD}  LocalStack POC — Setup Check${NC}"
  echo -e "${CYAN}${BOLD}═══════════════════════════════════════════${NC}"
  echo ""
}

check_command() {
  command -v "$1" &>/dev/null
}

# ── Docker ────────────────────────────────────────────────
check_docker() {
  echo -e "${BOLD}[Required] Docker${NC}"
  if check_command docker; then
    version=$(docker --version 2>/dev/null | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "  Version : ${version:-unknown}"
    if docker info &>/dev/null; then
      echo -e "  Daemon  : running"
      echo -e "  Status  : ${PASS}"
    else
      echo -e "  Daemon  : ${RED}not running${NC}"
      echo -e "  Status  : ${FAIL} — start Docker Desktop or the Docker daemon"
      required_ok=false
    fi
  else
    echo -e "  Status  : ${FAIL} — install from https://docs.docker.com/get-docker/"
    required_ok=false
  fi
  echo ""
}

# ── Docker Compose ────────────────────────────────────────
check_docker_compose() {
  echo -e "${BOLD}[Required] Docker Compose${NC}"
  if docker compose version &>/dev/null; then
    version=$(docker compose version 2>/dev/null | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "  Version : ${version:-unknown} (V2 plugin)"
    echo -e "  Status  : ${PASS}"
  elif check_command docker-compose; then
    version=$(docker-compose --version 2>/dev/null | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "  Version : ${version:-unknown} (V1 standalone)"
    echo -e "  Status  : ${PASS}"
  else
    echo -e "  Status  : ${FAIL} — install from https://docs.docker.com/compose/install/"
    required_ok=false
  fi
  echo ""
}

# ── Java ──────────────────────────────────────────────────
check_java() {
  echo -e "${BOLD}[Optional] Java JDK (for IDE / local development)${NC}"
  if check_command java; then
    version=$(java -version 2>&1 | head -1 | grep -oP '\d+' | head -1)
    echo -e "  Version : ${version:-unknown}"
    if [ "${version:-0}" -ge 21 ] 2>/dev/null; then
      echo -e "  Status  : ${PASS} (>= 21)"
    else
      echo -e "  Status  : ${WARN} — version 21+ recommended (current: ${version})"
    fi
  else
    echo -e "  Status  : ${WARN} — not installed (not needed for Docker builds)"
  fi
  echo ""
}

# ── Maven ─────────────────────────────────────────────────
check_maven() {
  echo -e "${BOLD}[Optional] Maven (for local builds outside Docker)${NC}"
  if check_command mvn; then
    version=$(mvn -version 2>/dev/null | head -1 | grep -oP '\d+\.\d+\.\d+' | head -1)
    echo -e "  Version : ${version:-unknown}"
    echo -e "  Status  : ${PASS}"
  else
    echo -e "  Status  : ${WARN} — not installed (Docker build includes Maven 3.9)"
  fi
  echo ""
}

# ── AWS CLI ───────────────────────────────────────────────
check_aws_cli() {
  echo -e "${BOLD}[Optional] AWS CLI (for direct LocalStack access)${NC}"
  if check_command aws; then
    version=$(aws --version 2>&1 | grep -oP '\d+\.\d+\.\d+' | head -1)
    major=$(echo "$version" | grep -oP '^\d+')
    echo -e "  Version : ${version:-unknown}"
    if [ "${major:-0}" -ge 2 ] 2>/dev/null; then
      echo -e "  Status  : ${PASS} (v2)"
    else
      echo -e "  Status  : ${WARN} — v2 recommended (current: v${major})"
    fi
  else
    echo -e "  Status  : ${WARN} — not installed (only needed for CLI debugging)"
  fi
  echo ""
}

# ── Postman ───────────────────────────────────────────────
check_postman() {
  echo -e "${BOLD}[Optional] Postman (for API testing)${NC}"
  if check_command postman; then
    echo -e "  Status  : ${PASS} — Postman CLI found"
  elif ls /Applications/Postman.app &>/dev/null 2>&1 || \
       ls "$LOCALAPPDATA/Postman" &>/dev/null 2>&1 || \
       ls /snap/postman &>/dev/null 2>&1; then
    echo -e "  Status  : ${PASS} — Postman desktop app found"
  else
    echo -e "  Status  : ${WARN} — not found (download from https://www.postman.com/downloads/)"
  fi
  echo ""
}

# ── .env file ─────────────────────────────────────────────
check_env_file() {
  echo -e "${BOLD}[Config] .env file${NC}"
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  if [ -f "$SCRIPT_DIR/.env" ]; then
    echo -e "  Status  : ${PASS} — .env exists"
  elif [ -f "$SCRIPT_DIR/.env.example" ]; then
    echo -e "  Status  : ${WARN} — .env missing"
    echo -e "  Creating .env from .env.example ..."
    cp "$SCRIPT_DIR/.env.example" "$SCRIPT_DIR/.env"
    echo -e "  Status  : ${PASS} — .env created from .env.example"
  else
    echo -e "  Status  : ${WARN} — neither .env nor .env.example found"
  fi
  echo ""
}

# ── Summary ───────────────────────────────────────────────
summary() {
  echo -e "${BOLD}───────────────────────────────────────────${NC}"
  if [ "$required_ok" = true ]; then
    echo -e "${GREEN}${BOLD}  All required tools are installed.${NC}"
    echo -e "  Run: ${CYAN}docker-compose up --build${NC}"
    echo ""
    exit 0
  else
    echo -e "${RED}${BOLD}  Some required tools are missing or not running.${NC}"
    echo -e "  Fix the ${RED}FAIL${NC} items above before proceeding."
    echo ""
    exit 1
  fi
}

# ── Main ──────────────────────────────────────────────────
header
check_docker
check_docker_compose
check_java
check_maven
check_aws_cli
check_postman
check_env_file
summary
