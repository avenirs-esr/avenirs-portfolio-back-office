#!/bin/bash

GREEN='\033[0;32m'
NC='\033[0m' # No Color

ALLOWED_IMPORTS=("java." "fr.avenirsesr.portfolio." "lombok." "org.slf4j.")

BASE_DIR="src/main/java/fr/avenirsesr/portfolio"

violations=0

is_allowed_import() {
  local import_line="$1"
  for prefix in "${ALLOWED_IMPORTS[@]}"; do
    if [[ "$import_line" == import\ $prefix* ]]; then
      return 0
    fi
  done
  return 1
}

echo "🔍 Checking imports in domain layer..."

while IFS= read -r -d '' domain_dir; do
  module_dir=$(basename "$(dirname "$domain_dir")")
  echo -n "📁 Module : $module_dir"

  module_violations=0

  # Use a simple for loop to avoid subshell scope issues
  for java_file in $(find "$domain_dir" -name "*.java"); do
    while read -r line; do
      [[ "$line" =~ ^import\  ]] || continue
      import_stmt="${line%;}"
      if ! is_allowed_import "$import_stmt"; then
        # shellcheck disable=SC2028
        echo -e -n "\n[$(basename "$java_file")] Forbidden import: $import_stmt"
        module_violations=$((module_violations + 1))
      fi
    done < "$java_file"
  done

  if [[ $module_violations -eq 0 ]]; then
    echo -e "  -  ${GREEN}✔ all clear${NC}"
  else
    echo -e "\n❌ $module_violations forbidden import(s) found"
  fi

  violations=$((violations + module_violations))
done < <(find "$BASE_DIR" -type d -name domain -print0)

if [[ $violations -gt 0 ]]; then
  echo -e "\n\033[1;31m× Forbidden imports were found.${NC}"
  exit 1
else
  echo -e "\n\033[1;32m🎉 All imports in domain layer are clean.${NC}"
  exit 0
fi