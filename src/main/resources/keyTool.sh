# OpenSSL (RSA key pair)
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Java-keytool
keytool -genkeypair -alias jwt-key -keyalg RSA -keysize 2048 -keystore jwt-keystore.p12 -storetype PKCS12 -storepass password