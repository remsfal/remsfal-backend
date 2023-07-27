# Generate a 2048-bit RSA private key
openssl genrsa -out rsaPrivateKey.pem 2048

# Generate the corresponding public key
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem

# Convert RSA private key to PKCS8 format
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem

# Set the permissions so only the user can read and write the key files
chmod 600 rsaPrivateKey.pem
chmod 600 privateKey.pem