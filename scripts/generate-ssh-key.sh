#!/usr/bin/env bash
set -euo pipefail

if ! command -v ssh-keygen >/dev/null 2>&1; then
  echo "ERROR: ssh-keygen is not installed."
  exit 1
fi

DEFAULT_EMAIL="$(git config --global user.email 2>/dev/null || true)"
EMAIL="${1:-${DEFAULT_EMAIL:-your_email@example.com}}"
KEY_NAME="${2:-id_ed25519}"
KEY_PATH="${HOME}/.ssh/${KEY_NAME}"

mkdir -p "${HOME}/.ssh"
chmod 700 "${HOME}/.ssh"

if [ -f "${KEY_PATH}" ] || [ -f "${KEY_PATH}.pub" ]; then
  echo "SSH key already exists: ${KEY_PATH}"
  if [ -f "${KEY_PATH}.pub" ]; then
    echo
    echo "Public key (add this to GitHub SSH keys):"
    cat "${KEY_PATH}.pub"
  fi
  exit 0
fi

echo "Generating SSH key..."
echo "  email: ${EMAIL}"
echo "  path : ${KEY_PATH}"

ssh-keygen -t ed25519 -C "${EMAIL}" -f "${KEY_PATH}" -N ""

chmod 600 "${KEY_PATH}"
chmod 644 "${KEY_PATH}.pub"

echo
echo "SSH key generated successfully."
echo "Public key (add this to GitHub SSH keys):"
cat "${KEY_PATH}.pub"
