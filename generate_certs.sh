#!/bin/bash

# Step 1: Navigate to UI directory and create certificates
echo "Creating certificates using mkcert..."
mkdir -p ovara-ui/certificates
cd ovara-ui/certificates || exit

mkcert localhost

# Step 2: Navigate back to project root
echo "Returning to project root..."
cd ../..

# Step 3: Create the keystore in the backend resources directory
echo "Creating the keystore for localhost..."

openssl pkcs12 -export \
    -in ovara-ui/certificates/localhost.pem \
    -inkey ovara-ui/certificates/localhost-key.pem \
    -out ovara-backend/src/main/resources/localhost-keystore.p12 \
    -name ovara-backend \
    -passout pass:ovarabackendkey

if [ $? -eq 0 ]; then
    echo "Keystore created successfully at ovara-backend/src/main/resources/localhost-keystore.p12"
else
    echo "Failed to create the keystore. Please check the logs above for errors."
fi