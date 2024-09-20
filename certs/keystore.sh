#!/bin/bash

# Function to prompt for input with a default value
prompt_input() {
  read -p "$1 [$2]: " input
  echo "${input:-$2}"
}

# Function to resolve the absolute path from a given path
resolve_path() {
  if [[ "$1" = /* ]]; then
    echo "$1"
  else
    echo "$(pwd)/$1"
  fi
}

# 1. Create a keystore using OpenSSL PKCS12
create_keystore() {
  cert_file=$(prompt_input "Enter the certificate file (.crt)" "cert_name.crt")
  key_file=$(prompt_input "Enter the private key file (.key)" "key_name.key")
  keystore_name=$(prompt_input "Enter the keystore output file (.p12/.jks)" "keystore_name")
  alias_name=$(prompt_input "Enter the alias name for the keystore" "name_of_your_store")
  keystore_password=$(prompt_input "Enter the keystore password" "")

  cert_file=$(resolve_path "$cert_file")
  key_file=$(resolve_path "$key_file")
  keystore_name=$(resolve_path "$keystore_name")

  echo "Creating keystore with OpenSSL..."
  openssl pkcs12 -export -in "$cert_file" -inkey "$key_file" -out "$keystore_name" -name "$alias_name" -password pass:"$keystore_password"
}

# 2. Create a truststore or import a certificate into a keystore using keytool
create_truststore() {
  cert_file=$(prompt_input "Enter the certificate file to import (.crt)" "cert_name")
  keystore_name=$(prompt_input "Enter the truststore/keystore file name (.jks/.p12)" "store_name")
  alias_name=$(prompt_input "Enter the alias name for the certificate" "alias_name")
  keystore_password=$(prompt_input "Enter the truststore/keystore password" "")

  cert_file=$(resolve_path "$cert_file")
  keystore_name=$(resolve_path "$keystore_name")

  echo "Creating truststore/importing certificate into keystore..."
  keytool -import -file "$cert_file" -alias "$alias_name" -keystore "$keystore_name" -storepass "$keystore_password" -noprompt
}

# 3. View the contents of a keystore using keytool
view_keystore() {
  keystore_path=$(prompt_input "Enter the path to the keystore (.jks or .p12)" "/path/to/keystore")
  keystore_password=$(prompt_input "Enter the keystore password" "")

  keystore_path=$(resolve_path "$keystore_path")

  echo "Viewing contents of the keystore..."
  keytool -v -list -keystore "$keystore_path" -storepass "$keystore_password"
}

# Main menu
while true; do
  echo "Select an operation:"
  echo "1) Create Keystore using OpenSSL"
  echo "2) Create Truststore or Import Certificate into Keystore"
  echo "3) View Keystore Contents"
  echo "4) Exit"
  read -p "Enter your choice: " choice

  case $choice in
    1) create_keystore ;;
    2) create_truststore ;;
    3) view_keystore ;;
    4) echo "Exiting."; break ;;
    *) echo "Invalid choice. Please select 1, 2, 3, or 4." ;;
  esac
done