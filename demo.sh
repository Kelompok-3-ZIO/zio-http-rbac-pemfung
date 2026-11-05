#!/usr/bin/env bash
# Empat skenario demo di slide 20. Jalankan server dulu di terminal lain:
#   scala-cli run src
set -u
BASE="${BASE:-http://localhost:8080}"
NILAI='{"mataKuliah":"Pemfung","huruf":"A"}'

langkah() {
  echo
  echo "== $1"
  echo "\$ ${*:2}"
  "${@:2}"
  echo
  read -r -p "(enter untuk lanjut) " _
}

langkah "1. Tanpa token, harus 401" \
  curl -s -i "$BASE/mahasiswa/3/nilai"

langkah "2. Mahasiswa mengubah nilai, harus 403" \
  curl -s -i -X PUT -H "Authorization: Bearer token-cici" -d "$NILAI" "$BASE/mahasiswa/3/nilai"

langkah "3. Dosen mengubah nilai, harus 200" \
  curl -s -i -X PUT -H "Authorization: Bearer token-budi" -d "$NILAI" "$BASE/mahasiswa/3/nilai"

langkah "4. Mahasiswa melihat nilai orang lain, harus 403" \
  curl -s -i -H "Authorization: Bearer token-cici" "$BASE/mahasiswa/4/nilai"
