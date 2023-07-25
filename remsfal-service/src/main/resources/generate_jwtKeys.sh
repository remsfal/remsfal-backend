mkdir jwt
openssl genrsa -out rsaPrivateKey.pem 2048 -ii
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
chmod 600 rsaPrivateKey.pem
chmod 600 privateKey.pem