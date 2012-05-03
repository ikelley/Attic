DESCRIPTION
-----------
Attic is a file sharing network that distributes input files to numerous "data centers" (DC) around the network.  Periodically the data centers contact the centralized "data lookup server" (DLS) to request files to replicate.  They then contact other data centers to download the file.  End users contact the DLS directly with their attic URL, and then the individual DCs to download the files.  To publish a file to attic, the java tools can be used, or alternatively the curl-based scripts can be used in conjunction with a "data seed" (DS) to seed the data to the network.

REQUIREMENTS: Java 5

FILE CRUD
---------
attic-get		: get a file from attic, specify multiple files
attic-put		: publish a file to attic (via data seed)
attic-remove		: remove a file from attic (via data seed)

SERVICES
--------
datacenter.sh		: start a data center, which shares files on the network
datalookupserver.sh	: start a data lookup server, which indexes files on the network
dataseed.sh		: start a data seed, which acts as a proxy for curl/java uploads to network, also a data center

Example of how to send data to an attic seed.

### Open Network using JSON ###
./attic-put -v -i -p EDGI -n ResearchResults.dat -d "The results of my research" /home/myuser/research.dat http://dataseed_hostname:7049/dp/seed

### Secure network using XML ###
./attic-put -v -i -x -p EDGI -n ResearchResults.dat -d "The results of my research" -c ./pem/attic-admin/attic-admin.cert -k ./pem/attic-admin/attic-admin -s ./pem/attic-seed/attic-seed.cert /home/myuser/research.dat http://dataseed_hostname:7049/dp/seed
