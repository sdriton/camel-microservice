KEYSTORE_PASSWORD=Camel
CLIENT_KEYSTORE_PASSWORD=Camel

# Create a keystore for the SERVER
keytool \
	-genkey \
	-keyalg RSA \
	-alias camel-server \
    -dname "CN=camel-mmicroservice.org, OU=Solution Architect, O=Geci, L=Montreal, ST=QC, C=CA" \
	-keystore camel-server.ks \
	-storepass $KEYSTORE_PASSWORD \
    -keypass $KEYSTORE_PASSWORD

# Export the SERVER certificate from the keystore
keytool \
	-export \
	-alias camel-server \
	-keystore camel-server.ks \
	-storepass $CLIENT_KEYSTORE_PASSWORD \
	-file camel-server.cer

# Create the CLIENT keystore
keytool \
	-genkey \
	-keyalg RSA \
	-alias camel-client \
    -dname "CN=camel-microservice.org, OU=Solution Architect, O=Geci, L=Montreal, ST=QC, C=CA" \
	-keystore camel-client.ks \
	-storepass $CLIENT_KEYSTORE_PASSWORD \
    -keypass $CLIENT_KEYSTORE_PASSWORD

# Import the previous exported certificate into a CLIENT truststore
keytool \
	-import \
	-alias camel-server \
	-keystore camel-client.ts \
	-file camel-server.cer \
	-storepass $CLIENT_KEYSTORE_PASSWORD \
    -trustcacerts \
    -noprompt

# OPTIONAL steps...
# If you want to make trusted also the client, you must export the client’s certificate from the keystore
# keytool -export -alias camel-client -keystore camel-client.ks -file camel-client.cer
# Import the client’s exported certificate into a broker SERVER truststore
# keytool -import -alias camel-client -keystore camel-server.ts -file camel-client.cer

# A good tool to know to list the contents of the key
#keytool -list -keystore camel-server.ks