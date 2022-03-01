**Config** 
  - contains the server port number 
  - path to a KV data file and index file (the index file contains the key and file-offset to data file for the KV line)

The Server and Client use raw Socket, with one thread-per-connection model.  The "key" is sent by the client with new-line terminated and the server sends back the "value" with new-line terminated. (hence, there is no new-line support in the value for this version)
