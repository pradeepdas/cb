**Config** 
  - contains the server port number 
  - path to a KV data file and index file (the index file contains the key and file-offset to data file for the KV line)

**Server and Client Communication**

The Server and Client use raw Socket, with one thread-per-connection model.  The "key" is sent by the client with new-line terminated and the server sends back the "value" with new-line terminated. (hence, there is no new-line support in the value for this version)

Server builds the index file for the first-time in case it's missing. The index file is simply a line with key and file-offset to line for that KV in the data file.

**Integration Test**
  - IntegrationTest tests with sequential and concurrent clients  


